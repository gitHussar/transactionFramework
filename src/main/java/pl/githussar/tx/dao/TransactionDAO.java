/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.githussar.tx.dao;

import pl.githussar.tx.entities.GlobalTransaction;
import pl.githussar.tx.entities.LocalTransaction;

import java.util.List;

public interface TransactionDAO {
	
	public GlobalTransaction create(GlobalTransaction globalTransaction);

	public LocalTransaction updateLocalTransaction(LocalTransaction localTransaction);



}
