public void testAutoscalingCapacity() {
    final long BYTES_IN_64GB = ByteSizeValue.ofGb(64).getBytes();
    final long AUTO_ML_MEMORY_FOR_64GB_NODE = NativeMemoryCalculator.allowedBytesForMl(BYTES_IN_64GB, randomIntBetween(5, 90), true);
    NativeMemoryCapacity capacity = ( new NativeMemoryCapacity(ByteSizeValue.ofGb(4).getBytes() - NATIVE_EXECUTABLE_CODE_OVERHEAD.getBytes(),ByteSizeValue.ofGb(1).getBytes() - NATIVE_EXECUTABLE_CODE_OVERHEAD.getBytes(),ByteSizeValue.ofMb(50).getBytes()));
    testAutoscalingCapacity(capacity, NativeMemoryCalculator.allowedBytesForMl(BYTES_IN_64GB,25,false), false);
testAutoscalingCapacity(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE, true);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementBelowJvmSizeKnotPoint(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveJvmSizeKnotPoint(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
    testAutoscalingCapacityWithUnknownJvmSizeAndMemoryRequirementAboveSingleNodeSize(capacity, AUTO_ML_MEMORY_FOR_64GB_NODE);
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

/** 
 * Tests the autoscaling capacity.
 * @param capacity the NativeMemoryCapacity object
 * @param memory the memory size
 * @param auto the auto scaling flag
 */
private void testAutoscalingCapacity(NativeMemoryCapacity capacity, long memory, boolean auto){
  int percentage = auto ? randomIntBetween(5,90) : 25;
  long expectedNodeSize = auto ? 1335885824L : ByteSizeValue.ofGb(1).getBytes() * 4L;
  long expectedTierSize = auto ? 4557111296L : ByteSizeValue.ofGb(4).getBytes() * 4L;
  MlMemoryAutoscalingCapacity autoscalingCapacity = capacity.autoscalingCapacity(percentage, auto, memory, 1).build();
  assertThat(autoscalingCapacity.nodeSize().getBytes(), equalTo(expectedNodeSize));
  assertThat(autoscalingCapacity.tierSize().getBytes(), equalTo(expectedTierSize));
}
