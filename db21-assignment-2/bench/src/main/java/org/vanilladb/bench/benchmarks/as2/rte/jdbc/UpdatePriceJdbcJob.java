package org.vanilladb.bench.benchmarks.as2.rte.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
import org.vanilladb.bench.remote.SutResultSet;
import org.vanilladb.bench.remote.jdbc.VanillaDbJdbcResultSet;
import org.vanilladb.bench.rte.jdbc.JdbcJob;
import org.vanilladb.bench.server.param.as2.UpdatePriceProcParamHelper;

public class UpdatePriceJdbcJob implements JdbcJob{
	private static Logger logger = Logger.getLogger(UpdatePriceJdbcJob.class
			.getName());
	
	
	@Override
	public SutResultSet execute(Connection conn, Object[] pars) throws SQLException {
		
		UpdatePriceProcParamHelper paramHelper = new UpdatePriceProcParamHelper();
		paramHelper.prepareParameters(pars);
		
		// Output message
		StringBuilder outputMsg = new StringBuilder("[");
		
		// Execute logic
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = null;
			
			// SELECT
			for (int i = 0; i < paramHelper.getReadCount(); i++) {
				int iid = paramHelper.getReadItemId(i);
				String sql = "SELECT i_name,i_price FROM item WHERE i_id = " + iid;
				rs = statement.executeQuery(sql);
				rs.beforeFirst();
				if (rs.next()) {
					//Check if the price exceeds As2BenchConstants.MAX_PRICE
					if(rs.getDouble("i_price")>As2BenchConstants.MAX_PRICE)
					{
						String sql_update = "UPDATE item SET i_price="+As2BenchConstants.MIN_PRICE+" WHERE i_id = " + iid;
						statement.executeUpdate(sql_update);
					}
					else
					{
						double new_price=rs.getDouble("i_price")+paramHelper.get_price_raise(i);
						String sql_update = "UPDATE item SET i_price="+new_price+" WHERE i_id = " + iid;
						statement.executeUpdate(sql_update);
					}
					outputMsg.append(String.format("'%s', ", rs.getString("i_name")));
				} else
					throw new RuntimeException("cannot find the record with i_id = " + iid);
				rs.close();
			}
			
			conn.commit();
			
			outputMsg.deleteCharAt(outputMsg.length() - 2);
			outputMsg.append("]");
			
			
			return new VanillaDbJdbcResultSet(true, outputMsg.toString());
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING))
				logger.warning(e.toString());
			return new VanillaDbJdbcResultSet(false, "");
		}
	}
}
