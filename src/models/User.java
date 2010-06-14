package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user")
public class User implements Serializable {
	private static final long serialVersionUID = 1074722286705578117L;

	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(name="name",length=255)
	private String name;
	
	@Column(name="surename",length=255)
	private String sureName;
	
	@Column(name="age")
	private Integer age;
	
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setSureName(String sureName) {
		this.sureName = sureName;
	}
	public String getSureName() {
		return sureName;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public Integer getAge() {
		return age;
	}
}
