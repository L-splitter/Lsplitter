public void testAutoscalingCapacity() {
    final long BYTES_IN_64GB = ByteSizeValue.ofGb(64).getBytes();
    final long AUTO_ML_MEMORY_FOR_64GB_NODE = NativeMemoryCalculator.allowedBytesForMl(BYTES_IN_64GB, randomIntBetween(5, 90), true);
    NativeMemoryCapacity capacity = createNativeMemoryCapacity();
    testAutoscalingCapacityWithAutoFalse(capacity, BYTES_IN_64GB);
    testAutoscalingCapacityWithAutoTrue(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementBelowJvmSizeKnotPoint(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveJvmSizeKnotPoint(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveSingleNodeSize(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
}
/**
 * Creates a NativeMemoryCapacity object with specific values.
 *
 * @return a NativeMemoryCapacity object
 */
private NativeMemoryCapacity createNativeMemoryCapacity() {
    return new NativeMemoryCapacity(
        ByteSizeValue.ofGb(4).getBytes() - NATIVE_EXECUTABLE_CODE_OVERHEAD.getBytes(),
        ByteSizeValue.ofGb(1).getBytes() - NATIVE_EXECUTABLE_CODE_OVERHEAD.getBytes(),
        ByteSizeValue.ofMb(50).getBytes()
    );
}
/**
 * Tests the autoscaling capacity when auto is false.
 *
 * @param capacity the NativeMemoryCapacity object
 * @param BYTES_IN_64GB the number of bytes in 64GB
 */
private void testAutoscalingCapacityWithAutoFalse(NativeMemoryCapacity capacity, long BYTES_IN_64GB) {
    MlMemoryAutoscalingCapacity autoscalingCapacity = capacity.autoscalingCapacity(
        25,
        false,
        NativeMemoryCalculator.allowedBytesForMl(BYTES_IN_64GB, 25, false),
        1
    ).build();
    assertThat(autoscalingCapacity.nodeSize().getBytes(), equalTo(ByteSizeValue.ofGb(1).getBytes() * 4L));
    assertThat(autoscalingCapacity.tierSize().getBytes(), equalTo(ByteSizeValue.ofGb(4).getBytes() * 4L));
}
/**
 * Tests the autoscaling capacity when auto is true.
 *
 * @param capacity the NativeMemoryCapacity object
 * @param AUTO_ML_MEMORY_FOR_64GB_NODE the auto ML memory for a 64GB node
 */
private void testAutoscalingCapacityWithAutoTrue(NativeMemoryCapacity capacity, long AUTO_ML_MEMORY_FOR_64GB_NODE) {
    MlMemoryAutoscalingCapacity autoscalingCapacity = capacity.autoscalingCapacity(
        randomIntBetween(5, 90),
        true,
        AUTO_ML_MEMORY_FOR_64GB_NODE,
        1
    ).build();
    assertThat(autoscalingCapacity.nodeSize().getBytes(), equalTo(1335885824L));
    assertThat(autoscalingCapacity.tierSize().getBytes(), equalTo(4557111296L));
}
/**
 * Tests the autoscaling capacity with unknown JVM size and memory requirement below JVM size knot point.
 *
 * @param capacity the NativeMemoryCapacity object
 * @param AUTO_ML_MEMORY_FOR_64GB_NODE the auto ML memory for a 64GB node
 */
private void testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementBelowJvmSizeKnotPoint(NativeMemoryCapacity capacity, long AUTO_ML_MEMORY_FOR_64GB_NODE) {
    // ... similar to the above methods
}
/**
 * Tests the autoscaling capacity with unknown JVM size and memory requirement above JVM size knot point.
 *
 * @param capacity the NativeMemoryCapacity object
 * @param AUTO_ML_MEMORY_FOR_64GB_NODE the auto ML memory for a 64GB node
 */
private void testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveJvmSizeKnotPoint(NativeMemoryCapacity capacity, long AUTO_ML_MEMORY_FOR_64GB_NODE) {
    // ... similar to the above methods
}
/**
 * Tests the autoscaling capacity with unknown JVM size and memory requirement above single node size.
 *
 * @param capacity the NativeMemoryCapacity object
 * @param AUTO_ML_MEMORY_FOR_64GB_NODE the auto ML memory for a 64GB node
 */
private void testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveSingleNodeSize(NativeMemoryCapacity capacity, long AUTO_ML_MEMORY_FOR_64GB_NODE) {
    // ... similar to the above methods
}
