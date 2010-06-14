package models;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the rack database table.
 * 
 */
@Entity
@Table(name="rack")
public class Rack implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(unique=true, nullable=false)
	private Integer id;

	@Column(nullable=false, length=255)
	private String description;

	@Column(nullable=false, length=255)
	private String name;

	@Column(nullable=false)
	private Integer place;

    public Rack() {
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPlace() {
		return this.place;
	}

	public void setPlace(Integer place) {
		this.place = place;
	}

}