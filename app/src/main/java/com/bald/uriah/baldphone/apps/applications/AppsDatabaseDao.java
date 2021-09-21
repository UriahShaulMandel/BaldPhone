/*
 * Copyright 2019 Uriah Shaul Mandel
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
 */

package com.bald.uriah.baldphone.apps.applications;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AppsDatabaseDao {
    @Query("SELECT * FROM App")
    List<App> getAll();

    @Query("SELECT * FROM App WHERE label LIKE :filter")
    List<App> getAllLike(String filter);

    @Query("SELECT * FROM App ORDER BY LOWER(label)")
    List<App> getAllOrderedByABC();

    @Query("SELECT * FROM App WHERE pinned = 1 ORDER BY label ASC")
    List<App> getAllPinned();

    @Query("UPDATE App SET pinned=:pinned WHERE id = :id")
    void update(int id, boolean pinned);

    @Query("SELECT * FROM App WHERE flatten_component_name LIKE :flattenComponentName LIMIT 1")
    App findByFlattenComponentName(String flattenComponentName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(App... apps);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<App> apps);

    @Delete
    void delete(App app);

    @Query("DELETE FROM App WHERE id IN (:appIds)")
    void deleteByIds(int... appIds);

    @Query("DELETE FROM App")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM App")
    int getNumberOfRows();
}
