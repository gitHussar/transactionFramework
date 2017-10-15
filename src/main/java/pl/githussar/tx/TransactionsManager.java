package pl.githussar.tx;

import java.util.List;


public class TransactionsManager {

	public Operation.Status processOperations(List<Operation> operations){
		TwoCommitsPhaseExceutor transactionExecutor = TwoCommitsPhaseExceutor.createInstance(operations);
		return transactionExecutor.executeTwoPhaseCommit();
	}

}
