/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.rules.coercer;

import com.facebook.buck.core.cell.nameresolver.CellNameResolver;
import com.facebook.buck.core.model.TargetConfiguration;
import com.facebook.buck.core.path.ForwardRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import java.util.Map;

public class SortedMapTypeCoercer<K extends Comparable<K>, V>
    implements TypeCoercer<Object, ImmutableSortedMap<K, V>> {
  private final TypeCoercer<Object, K> keyTypeCoercer;
  private final TypeCoercer<Object, V> valueTypeCoercer;
  private final TypeToken<ImmutableSortedMap<K, V>> typeToken;

  SortedMapTypeCoercer(
      TypeCoercer<Object, K> keyTypeCoercer, TypeCoercer<Object, V> valueTypeCoercer) {
    this.keyTypeCoercer = keyTypeCoercer;
    this.valueTypeCoercer = valueTypeCoercer;
    this.typeToken =
        new TypeToken<ImmutableSortedMap<K, V>>() {}.where(
                new TypeParameter<K>() {}, keyTypeCoercer.getOutputType())
            .where(new TypeParameter<V>() {}, valueTypeCoercer.getOutputType());
  }

  @Override
  public TypeToken<ImmutableSortedMap<K, V>> getOutputType() {
    return typeToken;
  }

  @Override
  public TypeToken<Object> getUnconfiguredType() {
    return TypeToken.of(Object.class);
  }

  @Override
  public boolean hasElementClass(Class<?>... types) {
    return keyTypeCoercer.hasElementClass(types) || valueTypeCoercer.hasElementClass(types);
  }

  @Override
  public void traverse(
      CellNameResolver cellRoots, ImmutableSortedMap<K, V> object, Traversal traversal) {
    traversal.traverse(object);
    for (Map.Entry<K, V> element : object.entrySet()) {
      keyTypeCoercer.traverse(cellRoots, element.getKey(), traversal);
      valueTypeCoercer.traverse(cellRoots, element.getValue(), traversal);
    }
  }

  @Override
  public Object coerceToUnconfigured(
      CellNameResolver cellRoots,
      ProjectFilesystem filesystem,
      ForwardRelativePath pathRelativeToProjectRoot,
      Object object)
      throws CoerceFailedException {
    return object;
  }

  @Override
  public ImmutableSortedMap<K, V> coerce(
      CellNameResolver cellRoots,
      ProjectFilesystem filesystem,
      ForwardRelativePath pathRelativeToProjectRoot,
      TargetConfiguration targetConfiguration,
      TargetConfiguration hostConfiguration,
      Object object)
      throws CoerceFailedException {
    if (object instanceof Map) {
      ImmutableSortedMap.Builder<K, V> builder = ImmutableSortedMap.naturalOrder();

      for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
        K key =
            keyTypeCoercer.coerce(
                cellRoots,
                filesystem,
                pathRelativeToProjectRoot,
                targetConfiguration,
                hostConfiguration,
                entry.getKey());
        V value =
            valueTypeCoercer.coerce(
                cellRoots,
                filesystem,
                pathRelativeToProjectRoot,
                targetConfiguration,
                hostConfiguration,
                entry.getValue());
        builder.put(key, value);
      }

      return builder.build();
    } else {
      throw CoerceFailedException.simple(object, getOutputType());
    }
  }
}
