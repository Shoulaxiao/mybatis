package org.apache.shoulaxiao.reflection;

/**
 * @author by shoulaxiao
 * @version 1.0.0
 * @Classname Person
 * @Description
 * @date 2023/2/9 14:34
 */
public class Person {

  private Long id;
  private String name;

  public Person() {
  }

  public String getName() {
    System.out.println("获取name");
    return name;
  }

  public void setName(String name) {
    System.out.println("设置name");
    this.name = name;
  }
}
