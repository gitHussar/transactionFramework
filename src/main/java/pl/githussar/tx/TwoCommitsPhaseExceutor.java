package pl.githussar.tx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import pl.githussar.tx.dao.TransactionDAO;
import pl.githussar.tx.dao.TransactionDAOJpa;
import pl.githussar.tx.entities.GlobalTransaction;
import pl.githussar.tx.entities.LocalTransaction;
import pl.githussar.tx.log.SerializableFactory;

public class TwoCommitsPhaseExceutor {

	private static final Logger logger = Logger.getLogger(TwoCommitsPhaseExceutor.class);
	
	private List<Operation> operations;
	
	private SerializableFactory serializableFactory = new SerializableFactory();

	private TransactionDAO transactionDAO;
	
	private TwoCommitsPhaseExceutor(List<Operation> operations, TransactionDAO transactionDAO){
		this.operations = operations;
		this.transactionDAO = transactionDAO;
	}
	
	public static TwoCommitsPhaseExceutor createInstance(List<Operation> operations, TransactionDAO transactionDAO){
		return new TwoCommitsPhaseExceutor(operations, transactionDAO);
	}
	
	public Operation.Status executeTwoPhaseCommit() throws TransactionException {
		
		GlobalTransaction globalTransaction =  beginTransaction(operations);
		
		executePreparationPhase(globalTransaction);
		
		boolean isError = globalTransaction.getLocalTransactions().stream()
				.anyMatch(element -> LocalTransaction.Status.ERROR.equals(element.getStatus()));
		
		logger.debug("transactionPrepared isError:"+isError);
		if (isError){			
			ErrorPhaseExecutor errorPhase = ErrorPhaseExecutor.createInstance(serializableFactory,transactionDAO);
			errorPhase.rollbackPreparedPhase(globalTransaction.getLocalTransactions()
					.stream()
					.filter(tx -> !LocalTransaction.Status.NEW.equals(tx.getStatus()))
					.collect(Collectors.toList()));
			return Operation.Status.ERROR;
		}
		return executeCommitPhase(globalTransaction);
	}
	
	private GlobalTransaction beginTransaction(List<Operation> operations) throws TransactionException{
		GlobalTransaction globalTransaction = null;
		try {
			globalTransaction = createAndSaveGloblaTransaction(operations);
			//TODO ORDER OF TRANSACTION
		} catch (Exception e){
			logger.error(e);
			throw new TransactionException("CREATION OF TRANSACTION FAILED");
		}
		return globalTransaction;
	}
	
	private GlobalTransaction createAndSaveGloblaTransaction(List<Operation> operations) throws IOException{
		GlobalTransaction globalTransaction = new GlobalTransaction();
		globalTransaction.setCode(UUID.randomUUID().toString());
		
		List<LocalTransaction> localTransactions = createLocalTransactionListFromOperations(
				operations, globalTransaction);
		
		globalTransaction.setLocalTransactions(localTransactions);
		globalTransaction = transactionDAO.create(globalTransaction);
		return globalTransaction;
	}
	
	private List<LocalTransaction> createLocalTransactionListFromOperations(List<Operation> operations
			, GlobalTransaction globalTransaction) throws IOException{
		
		List<LocalTransaction> localTransactions = new ArrayList<>();
		for(Operation operation : operations){
			LocalTransaction localTransaction = new LocalTransaction(globalTransaction
					, serializableFactory.serialize(operation)
					, UUID.randomUUID().toString()
					, LocalTransaction.Status.NEW);
			localTransactions.add(localTransaction);
		}
		return localTransactions;
	}

	private GlobalTransaction executePreparationPhase(GlobalTransaction globalTransaction) throws TransactionException{
		try {
			for (LocalTransaction localTransaction : globalTransaction.getLocalTransactions()){
				Operation operation = serializableFactory.deserialize(localTransaction.getOperation());
				Operation.Status operationStatus = operation.prepareTransaction(localTransaction.getOperationId());
				
				//metoda
				logger.debug("executePreparationPhase "+localTransaction.getOperationId() + " Status:"+operationStatus);
				localTransaction.setStatus(
						convertToLocalTransactionStatus(operationStatus, LocalTransaction.Status.PREPARED));
				transactionDAO.updateLocalTransaction(localTransaction);
	
				if (operationStatus == Operation.Status.ERROR){
					break;
				}
			}
		} catch (Exception e){
			logger.error(e);
			throw new TransactionException("PREPARATION PHASE FAILED");
		}
		
		return globalTransaction;
	}
	
	
	private Operation.Status executeCommitPhase(GlobalTransaction globalTransaction) throws TransactionException{
		Operation.Status returnStatus = Operation.Status.OK;
		
		try {
			for (LocalTransaction localTransaction : globalTransaction.getLocalTransactions() ){
				Operation operation = serializableFactory.deserialize(localTransaction.getOperation());
				Operation.Status operationStatus = operation.commit(localTransaction.getOperationId());
				
				//metoda
				logger.debug("executeCommitPhase "+localTransaction.getOperationId() + " Status:"+operationStatus);
				localTransaction.setStatus(
						convertToLocalTransactionStatus(operationStatus, LocalTransaction.Status.COMMITED));
				transactionDAO.updateLocalTransaction(localTransaction);
				
				if (operationStatus == Operation.Status.ERROR){
					returnStatus =  Operation.Status.ERROR;
					break;
				}
			}
		} catch (Exception e){
			logger.error(e);
			throw new TransactionException("COMMIT PHASE FAILED");
		}
		if (returnStatus == Operation.Status.ERROR){
			ErrorPhaseExecutor errorPhase = ErrorPhaseExecutor.createInstance(serializableFactory,transactionDAO);
			errorPhase.rollbackCommitedPhase(globalTransaction);
		}
		
		return returnStatus;
	}
	
	private LocalTransaction.Status convertToLocalTransactionStatus(
			Operation.Status operationStatus, LocalTransaction.Status success){
		
		return  Operation.Status.OK == operationStatus ? success : LocalTransaction.Status.ERROR;
	}

	public SerializableFactory getSerializableFactory() {
		return serializableFactory;
	}

	public void setSerializableFactory(SerializableFactory serializableFactory) {
		this.serializableFactory = serializableFactory;
	}

	public TransactionDAO getTransactionDAO() {
		return transactionDAO;
	}

	public void setTransactionDAO(TransactionDAO transactionDAO) {
		this.transactionDAO = transactionDAO;
	}

	
	
}
