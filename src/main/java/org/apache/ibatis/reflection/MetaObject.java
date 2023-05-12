/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author Clinton Begin
 */
public class MetaObject {

  /**
   * 持有的原对象
   */
  private final Object originalObject;
  /**
   * 包装对象，增强原始对象功能
   */
  private final ObjectWrapper objectWrapper;
  /**
   * 对象创建工厂。负责创建对象，对象可能嵌套
   */
  private final ObjectFactory objectFactory;
  /**
   * 包装对象创建工厂
   */
  private final ObjectWrapperFactory objectWrapperFactory;
  /**
   * 反射区工厂
   */
  private final ReflectorFactory reflectorFactory;

  /**
   * 私有构造
   * @param object 对象
   * @param objectFactory 对象创建工厂
   * @param objectWrapperFactory 包装对象创建工厂
   * @param reflectorFactory 反射器工厂
   */
  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;
    //如果对象实现了ObjectWrapper接口
    if (object instanceof ObjectWrapper) {
      this.objectWrapper = (ObjectWrapper) object;
    }
    //如果 ObjectWrapperFactory 已经对此对象进行加工，调用 getWrapperFor 方法获取加工对象
    else if (objectWrapperFactory.hasWrapperFor(object)) {
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    }
    // 如果对象是Map,则使用MapWrapper进行包装
    else if (object instanceof Map) {
      this.objectWrapper = new MapWrapper(this, (Map) object);
    }
    // 如果是集合Collection, 则使用CollectionWrapper进行包装
    else if (object instanceof Collection) {
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    }
    // 其他默认使用 BeanWrapper 作为加工对象
    else {
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  /**
   * 调用私有构造器
   * @param object 对象
   * @param objectFactory 对象创建工厂
   * @param objectWrapperFactory 包装对象创建工厂
   * @param reflectorFactory 反射器工厂
   * @return
   */
  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  public Object getValue(String name) {
    // 属性表达式解析
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // 如果有下一级
    if (prop.hasNext()) {
      // 构造其MetaObject对象
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      // 如果与 SystemMetaObject.NULL_META_OBJECT 相等，即传入的 NullObject.class
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return null;
      } else {
        // 否则递归调用
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      return objectWrapper.get(prop);
    }
  }

  public void setValue(String name, Object value) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        // 如果值是 null，就不用初始化下一级元素了
        if (value == null) {
          // don't instantiate child path if value is null
          return;
        } else {
          // 否则初始化 metaValue，然后递归调用
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      // 给指定找到最后一个对象，对其赋值
      objectWrapper.set(prop, value);
    }
  }

  /**
   *  根据传入 name 值构造 MateObject 对象
   */
  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  public void add(Object element) {
    objectWrapper.add(element);
  }

  public <E> void addAll(List<E> list) {
    objectWrapper.addAll(list);
  }

}
