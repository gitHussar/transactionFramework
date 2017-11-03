package pl.githussar.tx;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import pl.githussar.tx.dao.TransactionDAO;
import pl.githussar.tx.entities.GlobalTransaction;
import pl.githussar.tx.entities.LocalTransaction;
import pl.githussar.tx.log.SerializableFactory;

public class ErrorPhaseExecutor {

	static final Logger logger = Logger.getLogger(ErrorPhaseExecutor.class);
	
	private SerializableFactory serializableFactory = new SerializableFactory();
	
	private TransactionDAO transactionDAO;
	
	private ErrorPhaseExecutor(SerializableFactory serializableFactory, TransactionDAO transactionDAO){
		this.serializableFactory = serializableFactory;
		this.transactionDAO = transactionDAO;
	}
	
	public static ErrorPhaseExecutor createInstance(SerializableFactory serializableFactory, TransactionDAO transactionDAO){
		return new ErrorPhaseExecutor(serializableFactory, transactionDAO);
	}
	
	public void rollbackPreparedPhase(List<LocalTransaction> transactions) throws TransactionException{
		try {	
			for (LocalTransaction localTransaction : transactions ){
				Operation operation = serializableFactory.deserialize(localTransaction.getOperation());
				Operation.Status returnedStatus = operation.rollback(localTransaction.getOperationId());
				transactionDAO.updateLocalTransaction(localTransaction);
				if (returnedStatus == Operation.Status.ERROR){
					operation.revert(localTransaction.getOperationId());
				}
			}
		} catch (Exception e){
			logger.error(e);
			throw new TransactionException("ROLLACK PREPARATION PHASE FAILED");
		}
	}
	
	public void rollbackCommitedPhase(GlobalTransaction globalTransaction) throws TransactionException{
		executeRevertPhase(globalTransaction.getLocalTransactions().stream()
				.filter(element -> !LocalTransaction.Status.PREPARED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		rollbackPreparedPhase(globalTransaction.getLocalTransactions().stream()
				.filter(element -> LocalTransaction.Status.PREPARED.equals(element.getStatus()))
				.collect(Collectors.toList()));
		
	}

	private void executeRevertPhase(List<LocalTransaction> transactions)throws TransactionException{
		try{
			for (LocalTransaction localTransaction : transactions ){
				Operation operation = serializableFactory.deserialize(localTransaction.getOperation());
				Operation.Status returnedStatus = operation.revert(localTransaction.getOperationId());
				transactionDAO.updateLocalTransaction(localTransaction);
				if (returnedStatus == Operation.Status.ERROR){
					logger.error("Failed revert of operation:"+localTransaction.getOperationId());
				}
			}
		} catch (Exception e){
			logger.error(e);
			throw new TransactionException("REVERT PHASE FAILED");
		}
	}

	public SerializableFactory getSerializableFactory() {
		return serializableFactory;
	}

	public void setSerializableFactory(SerializableFactory serializableFactory) {
		this.serializableFactory = serializableFactory;
	}
}
