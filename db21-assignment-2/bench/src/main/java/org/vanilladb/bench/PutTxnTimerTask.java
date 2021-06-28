package org.vanilladb.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import org.vanilladb.bench.StatisticMgr;

public class PutTxnTimerTask extends TimerTask{
	
	List<TxnResultSet> result_set;
	
	@Override
	public void run() {
		result_set=new ArrayList<TxnResultSet>(StatisticMgr.my_resultSets);//copy
		StatisticMgr.resultSets_list.add(result_set);
		StatisticMgr.my_resultSets.clear();
	}

}
