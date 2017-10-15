package pl.githussar.tx;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TwoCommitsPhaseExceutor {

	private List<Operation> operations;

	public TwoCommitsPhaseExceutor(List<Operation> operations){
		this.operations = operations;
	}
	
	public static TwoCommitsPhaseExceutor createInstance(List<Operation> operations){
		return new TwoCommitsPhaseExceutor(operations);
	}
	
	public Operation.Status executeTwoPhaseCommit(){
		List<SingleTransaction> transactionPrepared = executePreparationPhase(operations);
		boolean isError = transactionPrepared.stream()
				.anyMatch(element -> SingleTransaction.Status.ERROR.equals(element.getStatus()));
		if (isError){
			ErrorPhaseExecutor errorPhase = ErrorPhaseExecutor.createInstance();
			errorPhase.rollbackPreparedPhase(transactionPrepared.stream()
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
		if (returnStatus == Operation.Status.ERROR){
			ErrorPhaseExecutor errorPhase = ErrorPhaseExecutor.createInstance();
			errorPhase.rollbackCommitedPhase(transactionPrepared);
		}
		
		return returnStatus;
	}
	
	
}
