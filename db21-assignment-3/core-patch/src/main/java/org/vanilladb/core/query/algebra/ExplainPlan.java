/*******************************************************************************
 * Copyright 2016, 2017 vanilladb.org contributors
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
package org.vanilladb.core.query.algebra;

import java.util.Set;

import org.vanilladb.core.sql.Schema;
import org.vanilladb.core.storage.metadata.statistics.Histogram;
import org.vanilladb.core.sql.Type;

/**
 * The {@link Plan} class corresponding to the <em>project</em> relational
 * algebra operator.
 */
public class ExplainPlan implements Plan {
	
	private Plan p;
	private Schema schema = new Schema();


	public ExplainPlan(Plan p) {
		this.p = p;
		schema.addField("query-plan", Type.VARCHAR(500));
	}


	@Override
	public Scan open() {
		String explainStr = explainStr(0);
		Scan s = p.open();
		int actual_rec = 0;
		s.beforeFirst();
		while(s.next()) {
			actual_rec ++;
		}
		explainStr += "\nActual #recs: " + actual_rec;
		return new ExplainScan(s, explainStr, schema);
	}


	@Override
	public long blocksAccessed() {
		return p.blocksAccessed();
	}


	@Override
	public Schema schema() {
		return schema;
	}

	@Override
	public Histogram histogram() {
		return p.histogram();
	}

	@Override
	public long recordsOutput() {
		return (long) p.recordsOutput();
	}
    
	@Override
	public String explainStr(int depth) {
		return "\n" + p.explainStr(depth);
	}

	
}
