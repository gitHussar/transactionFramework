package pl.githussar.tx;

public class SingleTransaction {

	private Operation.Status status;

	private String operationId;
	
	private Operation operation;

	public SingleTransaction(String operationId, Operation.Status status, Operation operation){
		this.operationId = operationId;
		this.status = status;
		this.operation = operation;
	}
	
	public Operation.Status getStatus() {
		return status;
	}

	public String getOperationId() {
		return operationId;
	}
	
	public Operation getOperation() {
		return operation;
	}
}
