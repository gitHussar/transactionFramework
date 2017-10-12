package pl.githussar.tx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionCoordinator {

	public List<Operation> operations;
	
	public TransactionCoordinator(List<Operation> operations){
		this.operations = operations;
	}
	
	public static TransactionCoordinator createInstance(List<Operation> operations){
		return new TransactionCoordinator(operations);
	}
	
	public Operation.Status executeTwoPhaseCommit(){
		List<SingleTransaction> transactionPrepared = null;
		try {
			transactionPrepared = executePreparationPhase(operations);
		} catch (TransactionException e){
			return Operation.Status.ERROR;
		}
		return executeCommitPhase(transactionPrepared);
	}
	
	private List<SingleTransaction> executePreparationPhase(List<Operation> operations) throws TransactionException{
		List<SingleTransaction> operationsPrepared = new ArrayList<>();
		for (Operation operation : operations ){
			String uniqueID = UUID.randomUUID().toString();
			Operation.Status restunStatus = operation.prepareTransaction(uniqueID);
			if (restunStatus == Operation.Status.ERROR){
				throw new TransactionException("PreparationPhase error");
			}
			SingleTransaction singleTranaction = new SingleTransaction(uniqueID, restunStatus, operation);
			operationsPrepared.add(singleTranaction);
		}
		return operationsPrepared;
	}
	
	private Operation.Status executeCommitPhase(List<SingleTransaction> transactionPrepared){
		if (transactionPrepared == null){
			return Operation.Status.ERROR;
		}
		for (SingleTransaction operationInfo : transactionPrepared ){
			Operation.Status restunStatus = operationInfo.getOperation().commit(operationInfo.getOperationId());
			if (restunStatus == Operation.Status.ERROR){
				//TODO
				return Operation.Status.ERROR;
			}
		}
		return Operation.Status.OK;
	}
}
