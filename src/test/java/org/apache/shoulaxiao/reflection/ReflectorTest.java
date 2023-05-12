package org.apache.shoulaxiao.reflection;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author by shoulaxiao
 * @version 1.0.0
 * @Classname ReflectorTest
 * @Description
 * @date 2023/2/9 14:33
 */
public class ReflectorTest {

  @Test
  public void testReflector() {
    ReflectorFactory factory = new DefaultReflectorFactory();
    Reflector reflector = factory.findForClass(Person.class);
    System.out.println("可读属性：" + Arrays.toString(reflector.getGetablePropertyNames()));
    System.out.println("可写属性：" + Arrays.toString(reflector.getSetablePropertyNames()));
    System.out.println("是否具有默认构造：" + reflector.hasDefaultConstructor());
    System.out.println("reflector对应的class：" + reflector.getType());
  }

  @Test
  public void testReflector01() throws InvocationTargetException, IllegalAccessException, InstantiationException {
    ReflectorFactory factory = new DefaultReflectorFactory();
    Reflector reflector = factory.findForClass(Person.class);
    Object o = reflector.getDefaultConstructor().newInstance();

    reflector.getSetInvoker("name").invoke(o, new Object[]{"sjhou"});
    reflector.getGetInvoker("name").invoke(o, null);
  }
}
