public void testVersionLocalTruncationUpdate() throws Exception {

	testVersionQuickUpdate();

	LocalManagedBufferFile vbf = null;
	LocalManagedBufferFile pbf = null;
	LocalManagedBufferFile saveFile = null;
	try {

		// Simulate checkout of version-1 (new private instance)
		privateTestFileMgr = new PrivateTestFileMgr(2);
		vbf = new LocalManagedBufferFile(versionedTestFileMgr, 1, -1);
		pbf = new LocalManagedBufferFile(vbf.getBufferSize(), privateTestFileMgr, PRIVATE);
		LocalBufferFile.copyFile(vbf, pbf, null, TaskMonitor.DUMMY);

		vbf.close();
		vbf = null;

		pbf.close();
		pbf = null;

		//
		// Modify private checkout file (with different changes than version-2 contains)
		// Truncation case: private file is longer than version-2 and must get truncated
		//

		// Open private file for version update
		pbf = new LocalManagedBufferFile(privateTestFileMgr, true, -1, PRIVATE);

		saveFile = (LocalManagedBufferFile) pbf.getSaveFile();
		assertNotNull(saveFile);

		// Write application level change file
		writeAppChangeFile(pbf);

		// Modify save file
		byte[] data = new byte[BUFFER_SIZE];
		DataBuffer buf = new DataBuffer(data);

		Arrays.fill(data, (byte) 0xf2);// modified empty buffer
		buf.setId(12);
		saveFile.put(buf, 2);

		Arrays.fill(data, (byte) 0xf3);// new buffer
		buf.setId(13);
		saveFile.put(buf, 3);

		Arrays.fill(data, (byte) 0xf4);// new buffer - added to free list below
		buf.setId(14);
		saveFile.put(buf, 4);

		Arrays.fill(data, (byte) 0xf5);// new buffer
		buf.setId(13);
		saveFile.put(buf, 5);

		// Set free ID list for output file
		int[] newFreeList = new int[] { 0, 4, 6 };
		saveFile.setFreeIndexes(newFreeList);

		// Copy/Set file parameters
		saveFile.setParameter("TestParm1", 0x320);
		saveFile.setParameter("TestParm2", 0x540);
		saveFile.setParameter("TestParm3", 0x670);

		pbf.saveCompleted(true);

		pbf.close();
		pbf = null;

		saveFile.close();
		saveFile = null;

		//
		// Perform update of private file to replicate version-2
		//

		// Open version-2
		vbf = new LocalManagedBufferFile(versionedTestFileMgr, false, -1, VERSIONED);

		// Reopen private file
		pbf = new LocalManagedBufferFile(privateTestFileMgr, true, -1, -1);

		// Perform quick update of private file to replicate version-2 file - should wipe-out all private changes
		pbf.updateFrom(vbf, 1, TaskMonitor.DUMMY);

		pbf.close();
		pbf = null;

		// Reopen to pickup update modifications
		pbf = new LocalManagedBufferFile(privateTestFileMgr, true, -1, -1);

		checkSameContent(vbf, pbf);

		vbf.close();
		vbf = null;

		pbf.close();
		pbf = null;

	}
	finally {
		if (saveFile != null) {
			try {
				saveFile.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
		if (vbf != null) {
			try {
				vbf.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
		if (pbf != null) {
			try {
				pbf.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
	}

	assertTrue("File handles may have failed to close properly",
		FileUtilities.deleteDir(testDir));
}