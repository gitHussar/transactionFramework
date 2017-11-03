package pl.githussar.tx.entities;


import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;



@Entity
public class LocalTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    public LocalTransaction(){
    	super();
    }
    
    public LocalTransaction(GlobalTransaction globalTransaction, byte[] operation, String operationId, Status status){
    	this.globalTransaction = globalTransaction;
    	this.operation = operation;
    	this.operationId = operationId;
    	this.status = status;
    }
    
    public enum Status {
    	NEW,
		PREPARED,
		COMMITED,
		ERROR,
	}
    
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int idc;
	
	@ManyToOne(fetch=FetchType.EAGER)
	private GlobalTransaction globalTransaction;
	
	private String operationId;

	@Lob
	@Column(length=100000)
	private byte[] operation;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	public int getIdc() {
		return idc;
	}

	public void setIdc(int idc) {
		this.idc = idc;
	}

	public GlobalTransaction getGlobalTransaction() {
		return globalTransaction;
	}

	public void setGlobalTransaction(GlobalTransaction globalTransaction) {
		this.globalTransaction = globalTransaction;
	}

	public byte[] getOperation() {
		return operation;
	}

	public void setOperation(byte[] operation) {
		this.operation = operation;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}


	
	
}
