package pl.githussar.tx;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class TransactionCoordinatorTest {

	
	@Test
	public void shouldReturnErrorStatusIfAnyOfOperationInPreparingPhaseSendError(){
		//given
		List<Operation> operations = prepareOperationFromPreparingPhaseWithErrorStatus();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.ERROR, status);
	}
	
	private List<Operation> prepareOperationFromPreparingPhaseWithErrorStatus(){
		List<Operation> operations = new ArrayList<>();
		Operation operation1 = Mockito.mock(Operation.class);
		when(operation1.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.OK);
		operations.add(operation1);
		Operation operation2 = Mockito.mock(Operation.class);
		when(operation2.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.ERROR);
		operations.add(operation2);
		
		return operations;
	}
	
	@Test
	public void shouldReturnSuccessStatusIfAllOfTwoPhaseOperationSendSuccess(){
		//given
		List<Operation> operations = prepareFullSuccessOperationList();
		
		//when
		TransactionCoordinator transactionCoordinator = new TransactionCoordinator(operations);
		Operation.Status status = transactionCoordinator.executeTwoPhaseCommit();
		
		//then
		Assert.assertEquals(Operation.Status.OK, status);
	}

	private List<Operation> prepareFullSuccessOperationList(){
		List<Operation> operations = new ArrayList<>();
		Operation operation1 = Mockito.mock(Operation.class);
		when(operation1.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operation1.commit(Matchers.anyString())).thenReturn(Operation.Status.OK);
		operations.add(operation1);
		Operation operation2 = Mockito.mock(Operation.class);
		when(operation2.prepareTransaction(Matchers.anyString())).thenReturn(Operation.Status.OK);
		when(operation2.commit(Matchers.anyString())).thenReturn(Operation.Status.ERROR);
		operations.add(operation2);
		
		return operations;
	}
}
