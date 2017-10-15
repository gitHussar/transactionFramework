package pl.githussar.tx;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ErrorPhaseExecutorTest {

	@Mock
	private Operation mockOperation;
	
	@Before
	public void setUp(){
		when(mockOperation.revert(anyString())).thenReturn(Operation.Status.OK);
	}
	
	@Test
	public void shouldCallRevertOnAllErrorInRollbackPreparedPhase(){
		//given
		List<SingleTransaction> transactionPrepared = prepareTransactionsForRollbackPreparedPhaseWithErrors();
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance();
		executorErrorPhase.rollbackPreparedPhase(transactionPrepared);
		
		//then
		Mockito.verify(mockOperation,Mockito.times(transactionPrepared.size())).revert(anyString());
	}
	
	@Test
	public void shouldCallRevertOnAlreadyCommitedOperationWhenErrorInCommitPhase(){
		//given
		List<SingleTransaction> transactionsCommited = prepareTransactionsForRollbackCommitedPhase(
				SingleTransaction.Status.COMMITED);
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance();
		executorErrorPhase.rollbackCommitedPhase(transactionsCommited);
		
		//then
		for (SingleTransaction tranasaction : transactionsCommited){
			Mockito.verify(tranasaction.getOperation(),Mockito.times(1)).revert(anyString());
		}
	}
	
	@Test
	public void shouldCallRollbackOnNotCommitedOperationWhenErrorInCommitPhase(){
		//given
		List<SingleTransaction> transactionsCommited = prepareTransactionsForRollbackCommitedPhase(
				SingleTransaction.Status.PREPARED);
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance();
		executorErrorPhase.rollbackCommitedPhase(transactionsCommited);
				
		//then
		for (SingleTransaction tranasaction : transactionsCommited){
			Mockito.verify(tranasaction.getOperation(),Mockito.times(1)).rollback(anyString());
		}
	}
	
	private List<SingleTransaction> prepareTransactionsForRollbackPreparedPhaseWithErrors(){
		List<SingleTransaction> transactionPrepared = new ArrayList<>();
		
		SingleTransaction singleTransaction = Mockito.mock(SingleTransaction.class);
		when(singleTransaction.getOperation()).thenReturn(mockOperation);
		when(mockOperation.rollback(anyString())).thenReturn(Operation.Status.ERROR);
		
		transactionPrepared.add(singleTransaction);
		transactionPrepared.add(singleTransaction);
		
		return transactionPrepared;
	}
	
	private List<SingleTransaction> prepareTransactionsForRollbackCommitedPhase(SingleTransaction.Status status){
		List<SingleTransaction> transactionCommited = new ArrayList<>();
		
		Operation mockOperationRevertFailed = Mockito.mock(Operation.class);
		when(mockOperationRevertFailed.revert(anyString())).thenReturn(Operation.Status.ERROR);
		
		SingleTransaction singleTransaction = Mockito.mock(SingleTransaction.class);
		when(singleTransaction.getOperation()).thenReturn(mockOperationRevertFailed);
		when(singleTransaction.getStatus()).thenReturn(status);
		
		
		SingleTransaction singleTransaction2 = Mockito.mock(SingleTransaction.class);
		when(singleTransaction2.getOperation()).thenReturn(mockOperation);
		when(singleTransaction2.getStatus()).thenReturn(status);
		
		transactionCommited.add(singleTransaction);
		transactionCommited.add(singleTransaction2);
		
		return transactionCommited;
	}
}
