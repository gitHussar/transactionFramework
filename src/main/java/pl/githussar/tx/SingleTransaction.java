package pl.githussar.tx;

public class SingleTransaction {

	enum Status {
		PREPARED,
		COMMITED,
		ERROR,
	}
	
	private Status status;

	private String operationId;
	
	private Operation operation;

	public SingleTransaction(String operationId, Status status, Operation operation){
		this.operationId = operationId;
		this.status = status;
		this.operation = operation;
	}

	public String getOperationId() {
		return operationId;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
