public void testVersionLocalTruncationUpdate() throws Exception {
    testVersionQuickUpdate();
    LocalManagedBufferFile vbf = null;
    LocalManagedBufferFile pbf = null;
    LocalManagedBufferFile saveFile = null;
    try {
        pbf = simulateCheckoutOfVersion(vbf, pbf);
        modifyPrivateCheckoutFile(pbf, saveFile);
        performUpdateOfPrivateFile(vbf, pbf);
    }
    finally {
        closeFiles(saveFile, vbf, pbf);
    }
    assertTrue("File handles may have failed to close properly",
        FileUtilities.deleteDir(testDir));
}
/**
 * Simulates the checkout of version-1 (new private instance)
 */
private LocalManagedBufferFile simulateCheckoutOfVersion(LocalManagedBufferFile vbf, LocalManagedBufferFile pbf) throws IOException {
    privateTestFileMgr = new PrivateTestFileMgr(2);
    vbf = new LocalManagedBufferFile(versionedTestFileMgr, 1, -1);
    pbf = new LocalManagedBufferFile(vbf.getBufferSize(), privateTestFileMgr, PRIVATE);
    LocalBufferFile.copyFile(vbf, pbf, null, TaskMonitor.DUMMY);
    vbf.close();
    vbf = null;
    pbf.close();
    pbf = null;
    // Open private file for version update
    pbf = new LocalManagedBufferFile(privateTestFileMgr, true, -1, PRIVATE);
    return pbf;
}
/**
 * Modifies the private checkout file
 */
private void modifyPrivateCheckoutFile(LocalManagedBufferFile pbf, LocalManagedBufferFile saveFile) throws IOException {
    saveFile = (LocalManagedBufferFile) pbf.getSaveFile();
    assertNotNull(saveFile);
    // Write application level change file
    writeAppChangeFile(pbf);
    // Modify save file
    modifySaveFile(saveFile);
    pbf.saveCompleted(true);
    pbf.close();
    pbf = null;
    saveFile.close();
    saveFile = null;
}
/**
 * Modifies the save file
 */
private void modifySaveFile(LocalManagedBufferFile saveFile) throws IOException {
    byte[] data = new byte[BUFFER_SIZE];
    DataBuffer buf = new DataBuffer(data);
    modifyBufferAndPutInFile(data, buf, saveFile, (byte) 0xf2, 12, 2);
    modifyBufferAndPutInFile(data, buf, saveFile, (byte) 0xf3, 13, 3);
    modifyBufferAndPutInFile(data, buf, saveFile, (byte) 0xf4, 14, 4);
    modifyBufferAndPutInFile(data, buf, saveFile, (byte) 0xf5, 13, 5);
    // Set free ID list for output file
    int[] newFreeList = new int[] { 0, 4, 6 };
    saveFile.setFreeIndexes(newFreeList);
    // Copy/Set file parameters
    saveFile.setParameter("TestParm1", 0x320);
    saveFile.setParameter("TestParm2", 0x540);
    saveFile.setParameter("TestParm3", 0x670);
}
/**
 * Modifies the buffer and puts it in the file
 */
private void modifyBufferAndPutInFile(byte[] data, DataBuffer buf, LocalManagedBufferFile saveFile, byte fillValue, int id, int index) throws IOException {
    Arrays.fill(data, fillValue);
    buf.setId(id);
    saveFile.put(buf, index);
}
/**
 * Performs the update of the private file to replicate version-2
 */
private void performUpdateOfPrivateFile(LocalManagedBufferFile vbf, LocalManagedBufferFile pbf) throws IOException {
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
/**
 * Closes the files
 */
private void closeFiles(LocalManagedBufferFile saveFile, LocalManagedBufferFile vbf, LocalManagedBufferFile pbf) {
    closeFile(saveFile);
    closeFile(vbf);
    closeFile(pbf);
}
/**
 * Closes a file
 */
private void closeFile(LocalManagedBufferFile file) {
    if (file != null) {
        try {
            file.close();
        }
        catch (IOException e) {
            // ignore
        }
    }
}
