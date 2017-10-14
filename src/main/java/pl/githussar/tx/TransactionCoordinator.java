package pl.githussar.tx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
				.anyMatch(element -> SingleTransaction.Status.ERROR.equals(element.getStatus()));
		if (isError){
			executeRollback(transactionPrepared.stream()
					.filter(element -> SingleTransaction.Status.PREPARED.equals(element.getStatus()))
					.collect(Collectors.toList()));
			return Operation.Status.ERROR;
		}
		return executeCommitPhase(transactionPrepared);
	}
	
	private List<SingleTransaction> executePreparationPhase(List<Operation> operations){
		List<SingleTransaction> operationsPrepared = new ArrayList<>();
		for (Operation operation : operations ){
			String uniqueID = UUID.randomUUID().toString();
			Operation.Status operationStatus = operation.prepareTransaction(uniqueID);
			SingleTransaction.Status transactionStatus = (Operation.Status.OK == operationStatus ? 
					SingleTransaction.Status.PREPARED : SingleTransaction.Status.ERROR);
			
			SingleTransaction singleTranaction = new SingleTransaction(uniqueID, transactionStatus, operation);
			operationsPrepared.add(singleTranaction);
			if (operationStatus == Operation.Status.ERROR){
				break;
			}
		}
		return operationsPrepared;
	}
	
	private Operation.Status executeCommitPhase(List<SingleTransaction> transactionPrepared){
		Operation.Status returnStatus = Operation.Status.OK;
		for (SingleTransaction transactionInfo : transactionPrepared ){
			Operation.Status operationStatus = transactionInfo.getOperation().commit(transactionInfo.getOperationId());
			SingleTransaction.Status transactionStatus = (Operation.Status.OK == operationStatus ? 
					SingleTransaction.Status.COMMITED : SingleTransaction.Status.ERROR);
			transactionInfo.setStatus(transactionStatus);
			if (operationStatus == Operation.Status.ERROR){
				returnStatus =  Operation.Status.ERROR;
				break;
			}
		}
		rollbackCommitedPhase(transactionPrepared, returnStatus == Operation.Status.ERROR);
		return returnStatus;
	}
	
	private void rollbackCommitedPhase(List<SingleTransaction> transactionPrepared, boolean rollback){
		if (rollback){
			executeInvertPhase(transactionPrepared.stream()
					.filter(element -> SingleTransaction.Status.COMMITED.equals(element.getStatus()))
					.collect(Collectors.toList()));
			executeRollback(transactionPrepared.stream()
					.filter(element -> SingleTransaction.Status.PREPARED.equals(element.getStatus()))
					.collect(Collectors.toList()));
		}
	}
	
	private Operation.Status executeRollback(List<SingleTransaction> transactionPrepared){
		Operation.Status returnStatus = Operation.Status.OK;
		for (SingleTransaction operationInfo : transactionPrepared ){
			Operation.Status returnedStatus = operationInfo.getOperation().rollback(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				operationInfo.getOperation().invert(operationInfo.getOperationId());
				returnStatus = Operation.Status.ERROR;
			}
		}
		return returnStatus;
	}
	
	private Operation.Status executeInvertPhase(List<SingleTransaction> transactions){
		for (SingleTransaction operationInfo : transactions ){
			Operation.Status returnedStatus = operationInfo.getOperation().invert(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				//TODO
				return Operation.Status.ERROR;
			}
		}
		return Operation.Status.OK;
	}
}
