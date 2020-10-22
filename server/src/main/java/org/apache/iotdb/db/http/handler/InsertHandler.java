/*
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
package org.apache.iotdb.db.http.handler;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.iotdb.db.auth.AuthException;
import org.apache.iotdb.db.exception.StorageEngineException;
import org.apache.iotdb.db.exception.metadata.IllegalPathException;
import org.apache.iotdb.db.exception.metadata.StorageGroupNotSetException;
import org.apache.iotdb.db.exception.query.QueryProcessException;
import org.apache.iotdb.db.http.constant.HttpConstant;
import org.apache.iotdb.db.metadata.PartialPath;
import org.apache.iotdb.db.qp.physical.crud.InsertRowPlan;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

public class InsertHandler extends Handler {
    public JsonElement handle(JsonArray json)
            throws IllegalPathException, QueryProcessException,
            StorageEngineException, StorageGroupNotSetException, AuthException {
        checkLogin();
        for (JsonElement o : json) {
            JsonObject object = o.getAsJsonObject();
            String deviceID = object.get(HttpConstant.DEVICE_ID).getAsString();
            JsonArray measurements = (JsonArray) object.get(HttpConstant.MEASUREMENTS);
            long timestamps = object.get(HttpConstant.TIMESTAMP).getAsLong();
            JsonArray values = (JsonArray) object.get(HttpConstant.VALUES);
            if (!insertByRow(deviceID, timestamps, getListString(measurements), values)) {
                throw new QueryProcessException(
                        String.format("%s can't be inserted successfully", deviceID));
            }
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(HttpConstant.RESULT, HttpConstant.SUCCESSFUL_OPERATION);
        return jsonObject;
    }

    private boolean insertByRow(String deviceId, long time, List<String> measurements, JsonArray values)
            throws IllegalPathException, QueryProcessException, StorageEngineException, StorageGroupNotSetException {
        InsertRowPlan plan = new InsertRowPlan();
        plan.setDeviceId(new PartialPath(deviceId));
        plan.setTime(time);
        plan.setMeasurements(measurements.toArray(new String[0]));
        plan.setDataTypes(new TSDataType[plan.getMeasurements().length]);
        List<String> valueList = new ArrayList<>();
        for (JsonElement value : values) {
            valueList.add(value.getAsString());
        }
        plan.setNeedInferType(true);
        plan.setValues(valueList.toArray(new String[0]));
        return executor.processNonQuery(plan);
    }

    /**
     * transform JsonArray to List<String>
     */
    private List<String> getListString(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (JsonElement o : jsonArray) {
            list.add(o.getAsString());
        }
        return list;
    }


}
