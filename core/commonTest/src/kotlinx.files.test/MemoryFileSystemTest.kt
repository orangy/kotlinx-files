package kotlinx.files.test

import kotlinx.files.*
import kotlinx.files.memory.*

class MemoryFileSystemTest : StandardFileSystemTest(MemoryFileSystem()) {
    
}
class MemoryPathTest : PathTest(MemoryFileSystem()) 