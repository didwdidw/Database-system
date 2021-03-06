/*******************************************************************************
 * Copyright 2016, 2018 vanilladb.org contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.vanilladb.bench;

import java.io.BufferedWriter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vanilladb.bench.util.BenchProperties;

class sort implements Comparator<Long>
{
	@Override
	public int compare(Long o1, Long o2) {
		return (int) (o1-o2);
	}
	
}

public class StatisticMgr {
	private static Logger logger = Logger.getLogger(StatisticMgr.class.getName());

	private static final File OUTPUT_DIR;


	static {
		String outputDirPath = BenchProperties.getLoader().getPropertyAsString(StatisticMgr.class.getName()
				+ ".OUTPUT_DIR", null);
		
		if (outputDirPath == null) {
			OUTPUT_DIR = new File(System.getProperty("user.home"), "benchmark_results");
		} else {
			OUTPUT_DIR = new File(outputDirPath);
		}

		// Create the directory if that doesn't exist
		if (!OUTPUT_DIR.exists())
			OUTPUT_DIR.mkdir();	
	}

	private static class TxnStatistic {
		private BenchTransactionType mType;
		private int txnCount = 0;
		private long totalResponseTimeNs = 0;

		public TxnStatistic(BenchTransactionType txnType) {
			this.mType = txnType;
		}

		public BenchTransactionType getmType() {
			return mType;
		}

		public void addTxnResponseTime(long responseTime) {
			txnCount++;
			totalResponseTimeNs += responseTime;
		}

		public int getTxnCount() {
			return txnCount;
		}

		public long getTotalResponseTime() {
			return totalResponseTimeNs;
		}
	}

	public static List<List<TxnResultSet>> resultSets_list = new ArrayList<List<TxnResultSet>>();
	public static  List<TxnResultSet> my_resultSets = new ArrayList<TxnResultSet>();
	private List<TxnResultSet> resultSets = new ArrayList<TxnResultSet>();
	private List<BenchTransactionType> allTxTypes;
	private String fileNamePostfix = "";
	private long recordStartTime = -1;
	
	public StatisticMgr(Collection<BenchTransactionType> txTypes) {
		allTxTypes = new LinkedList<BenchTransactionType>(txTypes);
	}
	
	public StatisticMgr(Collection<BenchTransactionType> txTypes, String namePostfix) {
		allTxTypes = new LinkedList<BenchTransactionType>(txTypes);
		fileNamePostfix = namePostfix;
	}
	
	/**
	 * We use the time that this method is called at as the start time for recording.
	 */
	public synchronized void setRecordStartTime() {
		if (recordStartTime == -1)
			{
				recordStartTime = System.nanoTime();
				Timer timer = new Timer();
				timer.schedule(new PutTxnTimerTask(),5000,5000);
			}
	}

	public synchronized void processTxnResult(TxnResultSet trs) {
		if (recordStartTime == -1)
			recordStartTime = trs.getTxnEndTime();
		resultSets.add(trs);
		
		//modify
		my_resultSets.add(trs);
	}

	public synchronized void outputReport() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss"); // E.g. "20210524-200824"
			String fileName = formatter.format(Calendar.getInstance().getTime());
			if (fileNamePostfix != null && !fileNamePostfix.isEmpty())
				fileName += "-" + fileNamePostfix; // E.g. "20210524-200824-postfix"
			
			outputDetailReport(fileName);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (logger.isLoggable(Level.INFO))
			logger.info("Finish creating benchmark report.");
	}
	
	private void outputDetailReport(String fileName) throws IOException {
		Map<BenchTransactionType, TxnStatistic> txnStatistics = new HashMap<BenchTransactionType, TxnStatistic>();
		Map<BenchTransactionType, Integer> abortedCounts = new HashMap<BenchTransactionType, Integer>();
		
		for (BenchTransactionType type : allTxTypes) {
			txnStatistics.put(type, new TxnStatistic(type));
			abortedCounts.put(type, 0);
		}
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(OUTPUT_DIR, fileName + ".txt")))) {
			// First line: total transaction count
			writer.write("# of txns (including aborted) during benchmark period: " + resultSets.size());
			writer.newLine();
			
			// Detail latency report
			/*for (TxnResultSet resultSet : resultSets) {
				if (resultSet.isTxnIsCommited()) {
					// Write a line: {[Tx Type]: [Latency]}
					writer.write(resultSet.getTxnType() + ": "
							+ TimeUnit.NANOSECONDS.toMillis(resultSet.getTxnResponseTime()) + " ms");
					writer.newLine();
					
					writer.write(resultSet.getOutMsg());
					writer.newLine();
					
					// Count transaction for each type
					TxnStatistic txnStatistic = txnStatistics.get(resultSet.getTxnType());
					txnStatistic.addTxnResponseTime(resultSet.getTxnResponseTime());
					
					
				} else {
					writer.write(resultSet.getTxnType() + ": ABORTED");
					writer.newLine();
					
					// Count transaction for each type
					Integer count = abortedCounts.get(resultSet.getTxnType());
					abortedCounts.put(resultSet.getTxnType(), count + 1);
				}
			}*/
			writer.write("time(sec), throughput(txs), avg_latency(ms), min(ms), max(ms), 25th_lat(ms), median_lat(ms), 75th_lat(ms)");
			writer.newLine();
			
			int time=0;
			for(List<TxnResultSet> result_sets:resultSets_list)
			{
				int throughput=0;
				long avg_res_time_Ms=0;
				time+=5;
				long total_res_time=0;
				List<Long> res_time_list=new ArrayList<Long>();
				long min_res;
				long max_res;
				long lat_25th;
				long lat_50th;
				long lat_75th;
				
				for(TxnResultSet rs:result_sets)
				{
					if (rs.isTxnIsCommited()) {
						
						throughput++;
						total_res_time+=rs.getTxnResponseTime();
						res_time_list.add(rs.getTxnResponseTime());
						
						// Count transaction for each type
						TxnStatistic txnStatistic = txnStatistics.get(rs.getTxnType());
						txnStatistic.addTxnResponseTime(rs.getTxnResponseTime());
					} else {
						// Count transaction for each type
						Integer count = abortedCounts.get(rs.getTxnType());
						abortedCounts.put(rs.getTxnType(), count + 1);
					}
				}
				
				Collections.sort(res_time_list,new sort());
				min_res=res_time_list.get(0);
				max_res=res_time_list.get(res_time_list.size()-1);
				lat_25th=res_time_list.get(res_time_list.size()/4);
				lat_50th=res_time_list.get(res_time_list.size()/2);
				lat_75th=res_time_list.get(res_time_list.size()/4*3);
				
				min_res=TimeUnit.NANOSECONDS.toMillis(min_res);
				max_res=TimeUnit.NANOSECONDS.toMillis(max_res);
				lat_25th=TimeUnit.NANOSECONDS.toMillis(lat_25th);
				lat_50th=TimeUnit.NANOSECONDS.toMillis(lat_50th);
				lat_75th=TimeUnit.NANOSECONDS.toMillis(lat_75th);
				
				avg_res_time_Ms = TimeUnit.NANOSECONDS.toMillis(
						total_res_time / throughput);
				
				writer.write(time+","+throughput+","+avg_res_time_Ms+","+min_res+","+max_res+","+lat_25th+","+lat_50th+","+lat_75th);
				writer.newLine();
			}
			
			// Last few lines: show the statistics for each type of transactions
			int abortedTotal = 0;
			for (Entry<BenchTransactionType, TxnStatistic> entry : txnStatistics.entrySet()) {
				TxnStatistic value = entry.getValue();
				int abortedCount = abortedCounts.get(entry.getKey());
				abortedTotal += abortedCount;
				long avgResTimeMs = 0;
				
				if (value.txnCount > 0) {
					avgResTimeMs = TimeUnit.NANOSECONDS.toMillis(
							value.getTotalResponseTime() / value.txnCount);
				}
				
				writer.write(value.getmType() + " - committed: " + value.getTxnCount() +
						", aborted: " + abortedCount + ", avg latency: " + avgResTimeMs + " ms");
				writer.newLine();
			}
			
			// Last line: Total statistics
			int finishedCount = resultSets.size() - abortedTotal;
			double avgResTimeMs = 0;
			if (finishedCount > 0) { // Avoid "Divide By Zero"
				for (TxnResultSet rs : resultSets)
					avgResTimeMs += rs.getTxnResponseTime() / finishedCount;
			}
			writer.write(String.format("TOTAL - committed: %d, aborted: %d, avg latency: %d ms", 
					finishedCount, abortedTotal, Math.round(avgResTimeMs / 1000000)));
		}
	}
}