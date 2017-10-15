package pl.githussar.tx;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ErrorPhaseExecutorTest {

	@Mock
	private Operation mockOperation;
	
	@Test
	public void shouldCallInvertOnAllErrorInRollbackPreparedPhase(){
		//given
		List<SingleTransaction> transactionPrepared = prepareTransactionsForRollbackPreparedPhaseWithErrors();
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance();
		executorErrorPhase.rollbackPreparedPhase(transactionPrepared);
		
		//then
		Mockito.verify(mockOperation,Mockito.times(transactionPrepared.size())).invert(anyString());
	}
	
	@Test
	public void shouldCallInvertOnAlreadyCommitedOperationWhenErrorInCommitPhase(){
		//given
		List<SingleTransaction> transactionsCommited = prepareTransactionsForRollbackCommitedPhase(
				SingleTransaction.Status.COMMITED);
		
		//when
		ErrorPhaseExecutor executorErrorPhase = ErrorPhaseExecutor.createInstance();
		executorErrorPhase.rollbackCommitedPhase(transactionsCommited);
		
		//then
		Mockito.verify(mockOperation,Mockito.times(transactionsCommited.size())).invert(anyString());
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
		Mockito.verify(mockOperation,Mockito.times(transactionsCommited.size())).rollback(anyString());
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
		
		SingleTransaction singleTransaction = Mockito.mock(SingleTransaction.class);
		when(singleTransaction.getOperation()).thenReturn(mockOperation);
		when(singleTransaction.getStatus()).thenReturn(status);
		
		transactionCommited.add(singleTransaction);
		transactionCommited.add(singleTransaction);
		
		return transactionCommited;
	}
}
