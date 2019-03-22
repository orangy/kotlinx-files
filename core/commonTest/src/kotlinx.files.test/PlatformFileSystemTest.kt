package kotlinx.files.test

import kotlinx.files.*

class PlatformFileSystemTest : StandardFileSystemTest(FileSystems.Default) 
class PlatformPathTest : PathTest(FileSystems.Default) 