package pl.githussar.tx;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

public class ErrorPhaseExecutor {

	static final Logger logger = Logger.getLogger(ErrorPhaseExecutor.class);
	
	public static ErrorPhaseExecutor createInstance(){
		return new ErrorPhaseExecutor();
	}
	
	public void rollbackPreparedPhase(List<SingleTransaction> transactionPrepared){
		for (SingleTransaction operationInfo : transactionPrepared ){
			Operation.Status returnedStatus = operationInfo.getOperation().rollback(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				operationInfo.getOperation().revert(operationInfo.getOperationId());
			}
		}
	}
	
	public void rollbackCommitedPhase(List<SingleTransaction> transactionPrepared){
		executeRevertPhase(transactionPrepared.stream()
				.filter(element -> SingleTransaction.Status.COMMITED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		rollbackPreparedPhase(transactionPrepared.stream()
				.filter(element -> SingleTransaction.Status.PREPARED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		
	}

	private void executeRevertPhase(List<SingleTransaction> transactions){
		for (SingleTransaction operationInfo : transactions ){
			Operation.Status returnedStatus = operationInfo.getOperation().revert(operationInfo.getOperationId());
			if (returnedStatus == Operation.Status.ERROR){
				logger.error("Failed revert of operation:"+operationInfo.getOperationId());
			}
		}
	}
}
