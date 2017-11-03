package pl.githussar.tx.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;



@Entity
public class GlobalTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int ido;
	
	private String code;
	
	//@OneToMany(mappedBy="globalTransaction")
	@OneToMany(mappedBy = "globalTransaction", cascade = CascadeType.PERSIST)
	private List<LocalTransaction> localTransactions = new ArrayList<>();

	
	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}

	public List<LocalTransaction> getLocalTransactions() {
		return localTransactions;
	}

	public void setLocalTransactions(List<LocalTransaction> localTransactions) {
		this.localTransactions = localTransactions;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
