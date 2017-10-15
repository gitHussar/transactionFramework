package pl.githussar.tx;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorPhaseExecutor {

	public static ErrorPhaseExecutor createInstance(){
		return new ErrorPhaseExecutor();
	}
	
	public Operation.Status rollbackPreparedPhase(List<SingleTransaction> transactionPrepared){
		Operation.Status returnStatus = Operation.Status.OK;
		for (SingleTransaction operationInfo : transactionPrepared ){
			Operation.Status returnedStatus = operationInfo.getOperation().rollback(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				operationInfo.getOperation().revert(operationInfo.getOperationId());
				returnStatus = Operation.Status.ERROR;
			}
		}
		return returnStatus;
	}
	
	public void rollbackCommitedPhase(List<SingleTransaction> transactionPrepared){
		executeRevertPhase(transactionPrepared.stream()
				.filter(element -> SingleTransaction.Status.COMMITED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		rollbackPreparedPhase(transactionPrepared.stream()
				.filter(element -> SingleTransaction.Status.PREPARED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		
	}

	private Operation.Status executeRevertPhase(List<SingleTransaction> transactions){
		for (SingleTransaction operationInfo : transactions ){
			Operation.Status returnedStatus = operationInfo.getOperation().revert(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				//TODO
				return Operation.Status.ERROR;
			}
		}
		return Operation.Status.OK;
	}
}
