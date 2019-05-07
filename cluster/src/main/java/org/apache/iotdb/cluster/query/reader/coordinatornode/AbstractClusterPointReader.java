/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.cluster.query.reader.coordinatornode;

import java.io.IOException;
import org.apache.iotdb.cluster.exception.RaftConnectionException;
import org.apache.iotdb.db.query.reader.IPointReader;
import org.apache.iotdb.db.utils.TimeValuePair;
import org.apache.iotdb.db.utils.TimeValuePairUtils;
import org.apache.iotdb.tsfile.read.common.BatchData;

/**
 * Cluster point reader
 */
public abstract class AbstractClusterPointReader implements IPointReader {

  /**
   * Current time value pair
   */
  protected TimeValuePair currentTimeValuePair;

  /**
   * Current batch data
   */
  protected BatchData currentBatchData;

  @Override
  public boolean hasNext() throws IOException {
    if (currentBatchData == null || !currentBatchData.hasNext()) {
      try {
        updateCurrentBatchData();
      } catch (RaftConnectionException e) {
        throw new IOException(e);
      }
      if (currentBatchData == null || !currentBatchData.hasNext()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Update current batch data. If necessary ,fetch batch data from remote query node
   */
  protected abstract void updateCurrentBatchData() throws RaftConnectionException;

  @Override
  public TimeValuePair next() throws IOException {
    if (hasNext()) {
      TimeValuePair timeValuePair = TimeValuePairUtils.getCurrentTimeValuePair(currentBatchData);
      currentTimeValuePair = timeValuePair;
      currentBatchData.next();
      return timeValuePair;
    }
    return null;
  }
}
