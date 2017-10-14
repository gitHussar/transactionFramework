package pl.githussar.tx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionCoordinator {

	private List<Operation> operations;
	
	public TransactionCoordinator(List<Operation> operations){
		this.operations = operations;
	}
	
	public static TransactionCoordinator createInstance(List<Operation> operations){
		return new TransactionCoordinator(operations);
	}
	
	public Operation.Status executeTwoPhaseCommit(){
		List<SingleTransaction> transactionPrepared = executePreparationPhase(operations);
		boolean isError = transactionPrepared.stream()
				.anyMatch(element -> Operation.Status.ERROR.equals(element.getStatus()));
		if (isError){
			executeRollbackOnlyForCorrectOpenedTranaction(transactionPrepared);
			return Operation.Status.ERROR;
		}
		return executeCommitPhase(transactionPrepared);
	}
	
	private List<SingleTransaction> executePreparationPhase(List<Operation> operations){
		List<SingleTransaction> operationsPrepared = new ArrayList<>();
		for (Operation operation : operations ){
			String uniqueID = UUID.randomUUID().toString();
			Operation.Status restunStatus = operation.prepareTransaction(uniqueID);
			SingleTransaction singleTranaction = new SingleTransaction(uniqueID, restunStatus, operation);
			operationsPrepared.add(singleTranaction);
			if (restunStatus == Operation.Status.ERROR){
				break;
			}
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
	
	private Operation.Status executeRollbackOnlyForCorrectOpenedTranaction(List<SingleTransaction> transactionPrepared){
		for (SingleTransaction operationInfo : transactionPrepared ){
			if (Operation.Status.OK.equals(operationInfo.getStatus())){
				Operation.Status restunStatus = operationInfo.getOperation().rollback(operationInfo.getOperationId());
				if (restunStatus == Operation.Status.ERROR){
					//TODO
					return Operation.Status.ERROR;
				}
			}
		}
		return Operation.Status.OK;
	}
}
