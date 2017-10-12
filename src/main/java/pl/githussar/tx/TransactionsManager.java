package pl.githussar.tx;

import java.util.List;


public class TransactionsManager {

	public Operation.Status processOperations(List<Operation> operations){
		TransactionCoordinator coorfinatorTx = TransactionCoordinator.createInstance(operations);
		return coorfinatorTx.executeTwoPhaseCommit();
	}

}
