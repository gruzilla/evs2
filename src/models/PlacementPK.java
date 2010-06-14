package models;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the placement database table.
 * 
 */
@Embeddable
public class PlacementPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="item_id", unique=true, nullable=false)
	private Integer itemId;

	@Column(name="rack_id", unique=true, nullable=false)
	private Integer rackId;

    public PlacementPK() {
    }
	public Integer getItemId() {
		return this.itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public Integer getRackId() {
		return this.rackId;
	}
	public void setRackId(Integer rackId) {
		this.rackId = rackId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PlacementPK)) {
			return false;
		}
		PlacementPK castOther = (PlacementPK)other;
		return 
			(this.itemId == castOther.itemId)
			&& (this.rackId == castOther.rackId);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.itemId;
		hash = hash * prime + this.rackId;
		
		return hash;
    }
}