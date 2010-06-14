package models;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the placement database table.
 * 
 */
@Entity
@Table(name="placement")
public class Placement implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PlacementPK id;

	@Column(nullable=false)
	private Integer amount;

	@Column(name="storing_position", nullable=false, length=255)
	private String storingPosition;

	//uni-directional many-to-one association to Rack
	@ManyToOne(cascade={CascadeType.REMOVE}, fetch=FetchType.LAZY)
	@JoinColumn(name="rack_id", nullable=false, insertable=false, updatable=false)
	private Rack rack;

	//uni-directional many-to-one association to Item
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="item_id", nullable=false, insertable=false, updatable=false)
	private Item item;

    public Placement() {
    }

	public PlacementPK getId() {
		return this.id;
	}

	public void setId(PlacementPK id) {
		this.id = id;
	}
	
	public Integer getAmount() {
		return this.amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getStoringPosition() {
		return this.storingPosition;
	}

	public void setStoringPosition(String storingPosition) {
		this.storingPosition = storingPosition;
	}

	public Rack getRack() {
		return this.rack;
	}

	public void setRack(Rack rack) {
		this.rack = rack;
	}
	
	public Item getItem() {
		return this.item;
	}

	public void setItem(Item item) {
		this.item = item;
	}
	
}