/*
 * Syncany, www.syncany.org
 * Copyright (C) 2011-2013 Philipp C. Heckel <philipp.heckel@gmail.com> 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.syncany.tests.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.syncany.tests.util.TestAssertUtil.assertFileListEquals;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.syncany.cli.CommandLineClient;
import org.syncany.tests.util.TestCliUtil;
import org.syncany.tests.util.TestConfigUtil;
import org.syncany.tests.util.TestFileUtil;

public class UpCommandTest {	
	@Test
	public void testCliSyncUpWithoutCleanup() throws Exception {
		Map<String, String> connectionSettings = TestConfigUtil.createTestLocalConnectionSettings();
		Map<String, String> clientA = TestCliUtil.createLocalTestEnvAndInit("A", connectionSettings);

		new CommandLineClient(new String[] { 
			 "--localdir", clientA.get("localdir"),
			 "up",
			 "--no-cleanup" 
		}).start();

		for (int i=1; i<=20; i++) {
			new File(clientA.get("localdir")+"/somefolder"+i).mkdir();

			new CommandLineClient(new String[] { 
				"--localdir", clientA.get("localdir"),
				"up",
				"--no-cleanup"
			}).start();
		}
		
		for (int i=1; i<=20; i++) {
			File databaseFileInRepo = new File(connectionSettings.get("path")+"/db-A-"+i);			
			assertTrue("Database file SHOULD exist: "+databaseFileInRepo, databaseFileInRepo.exists());
		}
				
		TestCliUtil.deleteTestLocalConfigAndData(clientA);		
	}	
	
	@Test
	public void testCliSyncUpWithCleanup() throws Exception {
		Map<String, String> connectionSettings = TestConfigUtil.createTestLocalConnectionSettings();
		Map<String, String> clientA = TestCliUtil.createLocalTestEnvAndInit("A", connectionSettings);

		new CommandLineClient(new String[] { 
			 "--localdir", clientA.get("localdir"),
			 "up" 
		}).start();

		for (int i=1; i<=20; i++) {
			new File(clientA.get("localdir")+"/somefolder"+i).mkdir();

			new CommandLineClient(new String[] { 
				"--localdir", clientA.get("localdir"),
				"up"
			}).start();
		}
		

		for (int i=1; i<=10; i++) {
			File databaseFileInRepo = new File(connectionSettings.get("path")+"/db-A-"+i);			
			assertFalse("Database file SHOULD NOT exist: "+databaseFileInRepo, databaseFileInRepo.exists());
		}
		
		for (int i=11; i<=20; i++) {
			File databaseFileInRepo = new File(connectionSettings.get("path")+"/db-A-"+i);			
			assertTrue("Database file SHOULD exist: "+databaseFileInRepo, databaseFileInRepo.exists());
		}
				
		TestCliUtil.deleteTestLocalConfigAndData(clientA);		
	}	
	
	@Test
	public void testCliSyncDownNoArgs() throws Exception {
		Map<String, String> connectionSettings = TestConfigUtil.createTestLocalConnectionSettings();
		Map<String, String> clientA = TestCliUtil.createLocalTestEnvAndInit("A", connectionSettings);
		Map<String, String> clientB = TestCliUtil.createLocalTestEnvAndConnect("B", connectionSettings);

		TestFileUtil.createRandomFilesInDirectory(new File(clientA.get("localdir")), 20*1024, 10);
				
		new CommandLineClient(new String[] { 
			 "--localdir", clientA.get("localdir"),
			 "up" 
		}).start();

		new CommandLineClient(new String[] { 
			 "--localdir", clientB.get("localdir"),
			 "down" 
		}).start();
		
		assertFileListEquals(new File(clientA.get("localdir")), new File(clientB.get("localdir")));
		
		// TODO [medium] Write asserts for 'down' output
		
		TestCliUtil.deleteTestLocalConfigAndData(clientA);		
		TestCliUtil.deleteTestLocalConfigAndData(clientB);
	}	
		
	public String toString(ByteArrayOutputStream bos) {
		return new String(bos.toByteArray());
	}
	
	public String[] toStringArray(ByteArrayOutputStream bos) {		
		return toString(bos).split("[\\r\\n]+|[\\n\\r]+|[\\n]+");
	}
}
