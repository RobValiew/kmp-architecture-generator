package com.vali.kmpgenerator

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CreateKmpFeatureTask : DefaultTask() {

    private var featureName: String = ""
    private var architectureName: String? = null
    private var packageNameOption: String? = null
    private var outputDirOption: String? = null
    private var diName: String? = null
    private var recreate: Boolean = false
    private var networkingName: String? = null

    @Option(
        option = "name",
        description = "Name of feature module. Examples: Profile, Products, Settings"
    )
    fun setFeatureName(value: String) {
        featureName = value
    }

    @Option(
        option = "architecture",
        description = "Architecture type: mvi or mvvm"
    )
    fun setArchitecture(value: String) {
        architectureName = value
    }

    @Option(
        option = "packageName",
        description = "Base package name. Example: com.example.shared"
    )
    fun setPackageName(value: String) {
        packageNameOption = value
    }

    @Option(
        option = "outputDir",
        description = "Output source directory. Default: src/commonMain/kotlin"
    )
    fun setOutputDir(value: String) {
        outputDirOption = value
    }

    @Option(
        option = "di",
        description = "DI type: none or koin"
    )
    fun setDi(value: String) {
        diName = value
    }

    @Option(
        option = "recreate",
        description = "Delete existing generated feature folder and create it again"
    )
    fun setRecreate(value: Boolean) {
        recreate = value
    }

    @Option(
        option = "networking",
        description = "Networking type: none or ktor"
    )
    fun setNetworking(value: String) {
        networkingName = value
    }

    @TaskAction
    fun generate() {
        require(featureName.isNotBlank()) {
            "Feature name is required. Example: ./gradlew createKmpFeature --name Profile"
        }

        val extension = project.extensions.findByType(
            KmpArchitectureGeneratorExtension::class.java
        ) ?: KmpArchitectureGeneratorExtension()

        val resolvedArchitectureName = architectureName ?: extension.defaultArchitecture
        val resolvedPackageName = packageNameOption ?: extension.packageName
        val resolvedOutputDir = outputDirOption ?: extension.outputDir
        val resolvedDiName = diName ?: extension.defaultDi
        val resolvedNetworkingName = networkingName ?: extension.defaultNetworking


        val architecture = Architecture.from(resolvedArchitectureName)
        val di = Di.from(resolvedDiName)
        val networking = Networking.from(resolvedNetworkingName)

        val classPrefix = featureName.toPascalCase()
        val featurePackageSegment = featureName.toPackageSegment()

        val baseDirectory = File(
            project.projectDir,
            "$resolvedOutputDir/${resolvedPackageName.replace(".", "/")}/features/$featurePackageSegment"
        )

        val featurePackage = "$resolvedPackageName.features.$featurePackageSegment"

        generateNetworkCoreIfNeeded(
            outputDir = resolvedOutputDir,
            packageName = resolvedPackageName,
            networking = networking,
            di = di
        )

        recreateFeatureDirectoryIfNeeded(
            baseDirectory = baseDirectory,
            classPrefix = classPrefix
        )

        writeMarkerFile(
            baseDirectory = baseDirectory,
            classPrefix = classPrefix,
            architecture = architecture,
            di = di
        )

        when (architecture) {
            Architecture.MVI -> generateMviFeature(
                baseDirectory = baseDirectory,
                packageName = featurePackage,
                classPrefix = classPrefix,
                di = di,
                networking = networking
            )

            Architecture.MVVM -> generateMvvmFeature(
                baseDirectory = baseDirectory,
                packageName = featurePackage,
                classPrefix = classPrefix,
                di = di,
                networking = networking
            )
        }

        logger.lifecycle(
            "Generated $architecture feature '$classPrefix' with DI '$di' in: ${baseDirectory.absolutePath}"
        )
    }

    private fun recreateFeatureDirectoryIfNeeded(
        baseDirectory: File,
        classPrefix: String
    ) {
        if (!recreate) {
            return
        }

        if (!baseDirectory.exists()) {
            logger.lifecycle(
                "Recreate requested for '$classPrefix', but feature directory does not exist yet."
            )
            return
        }

        val markerFile = File(baseDirectory, ".kmp-feature-generator")

        if (!markerFile.exists()) {
            error(
                "Cannot recreate feature '$classPrefix'. " +
                        "Directory exists, but marker file was not found: ${markerFile.absolutePath}. " +
                        "This folder may contain manually written code. Delete it manually if you are sure."
            )
        }

        logger.lifecycle(
            "Recreating feature '$classPrefix'. Existing generated folder will be deleted: ${baseDirectory.absolutePath}"
        )

        val deleted = baseDirectory.deleteRecursively()

        if (!deleted) {
            error(
                "Failed to delete existing feature directory: ${baseDirectory.absolutePath}"
            )
        }
    }
    private fun generateNetworkCoreIfNeeded(
        outputDir: String,
        packageName: String,
        networking: Networking,
        di: Di
    ) {
        if (networking != Networking.KTOR) {
            return
        }

        val networkDirectory = File(
            project.projectDir,
            "$outputDir/${packageName.replace(".", "/")}/core/network"
        )

        writeFile(
            file = File(networkDirectory, "HttpClientFactory.kt"),
            content = generateHttpClientFactory(packageName)
        )

        if (di == Di.KOIN) {
            writeFile(
                file = File(networkDirectory, "NetworkModule.kt"),
                content = generateNetworkModule(packageName)
            )
        }
    }
    private fun generateHttpClientFactory(
        packageName: String
    ): String {
        return """
        package $packageName.core.network

        import io.ktor.client.HttpClient
        import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
        import io.ktor.serialization.kotlinx.json.json
        import kotlinx.serialization.json.Json

        object HttpClientFactory {

            fun create(): HttpClient {
                return HttpClient {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                explicitNulls = false
                            }
                        )
                    }
                }
            }
        }
    """.trimIndent()
    }
    private fun generateNetworkModule(
        packageName: String
    ): String {
        return """
        package $packageName.core.network

        import org.koin.dsl.module

        val networkModule = module {
            single {
                HttpClientFactory.create()
            }
        }
    """.trimIndent()
    }
    private fun writeMarkerFile(
        baseDirectory: File,
        classPrefix: String,
        architecture: Architecture,
        di: Di
    ) {
        val markerFile = File(baseDirectory, ".kmp-feature-generator")

        if (markerFile.exists()) {
            val markerText = markerFile.readText()

            val architectureLine = markerText
                .lineSequence()
                .firstOrNull { line -> line.startsWith("architecture=") }

            val existingArchitecture = architectureLine
                ?.substringAfter("architecture=")
                ?.trim()

            if (
                existingArchitecture != null &&
                !existingArchitecture.equals(architecture.name, ignoreCase = true)
            ) {
                error(
                    "Feature '$classPrefix' was already generated with architecture '$existingArchitecture'. " +
                            "You are trying to generate it with '${architecture.name}'. " +
                            "Use another feature name or delete the existing feature folder first."
                )
            }

            logger.lifecycle("Feature marker already exists: ${markerFile.absolutePath}")
            return
        }

        markerFile.parentFile.mkdirs()
        markerFile.writeText(
            """
            name=$classPrefix
            architecture=${architecture.name}
            di=${di.name}
        """.trimIndent()
        )

        logger.lifecycle("Created marker file: ${markerFile.absolutePath}")
    }

    private fun generateMviFeature(
        baseDirectory: File,
        packageName: String,
        classPrefix: String,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}State.kt"),
            content = """
                package $packageName.presentation

                data class ${classPrefix}State(
                    val isLoading: Boolean = false,
                    val error: String? = null
                )
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}Action.kt"),
            content = """
                package $packageName.presentation

                sealed interface ${classPrefix}Action {
                    data object Load : ${classPrefix}Action
                    data object Retry : ${classPrefix}Action
                }
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}Result.kt"),
            content = """
                package $packageName.presentation

                sealed interface ${classPrefix}Result {
                    data object Loading : ${classPrefix}Result
                    data object Success : ${classPrefix}Result
                    data class Error(val message: String) : ${classPrefix}Result
                }
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}Effect.kt"),
            content = """
                package $packageName.presentation

                sealed interface ${classPrefix}Effect {
                    data class ShowMessage(val message: String) : ${classPrefix}Effect
                }
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}Reducer.kt"),
            content = """
                package $packageName.presentation

                class ${classPrefix}Reducer {

                    fun reduce(
                        currentState: ${classPrefix}State,
                        result: ${classPrefix}Result
                    ): ${classPrefix}State {
                        return when (result) {
                            ${classPrefix}Result.Loading -> {
                                currentState.copy(
                                    isLoading = true,
                                    error = null
                                )
                            }

                            ${classPrefix}Result.Success -> {
                                currentState.copy(
                                    isLoading = false,
                                    error = null
                                )
                            }

                            is ${classPrefix}Result.Error -> {
                                currentState.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}ViewModel.kt"),
            content = """
                package $packageName.presentation

                import $packageName.domain.${classPrefix}UseCase
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.Dispatchers
                import kotlinx.coroutines.SupervisorJob
                import kotlinx.coroutines.cancel
                import kotlinx.coroutines.flow.MutableSharedFlow
                import kotlinx.coroutines.flow.MutableStateFlow
                import kotlinx.coroutines.flow.SharedFlow
                import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asSharedFlow
                import kotlinx.coroutines.flow.asStateFlow
                import kotlinx.coroutines.launch

                class ${classPrefix}ViewModel(
                    private val useCase: ${classPrefix}UseCase,
                    private val reducer: ${classPrefix}Reducer = ${classPrefix}Reducer()
                ) {

                    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

                    private val _state = MutableStateFlow(${classPrefix}State())
                    val state: StateFlow<${classPrefix}State> = _state.asStateFlow()

                    private val _effect = MutableSharedFlow<${classPrefix}Effect>()
                    val effect: SharedFlow<${classPrefix}Effect> = _effect.asSharedFlow()

                    fun onAction(action: ${classPrefix}Action) {
                        when (action) {
                            ${classPrefix}Action.Load -> load()
                            ${classPrefix}Action.Retry -> load()
                        }
                    }

                    private fun load() {
                        viewModelScope.launch {
                            reduce(${classPrefix}Result.Loading)

                            try {
                                useCase()
                                reduce(${classPrefix}Result.Success)
                            } catch (exception: Exception) {
                                val message = exception.message ?: "Unknown error"
                                reduce(${classPrefix}Result.Error(message))
                                _effect.emit(${classPrefix}Effect.ShowMessage(message))
                            }
                        }
                    }

                    private fun reduce(result: ${classPrefix}Result) {
                        val currentState = _state.value
                        val newState = reducer.reduce(currentState, result)
                        _state.value = newState
                    }

                    fun clear() {
                        viewModelScope.cancel()
                    }
                }
            """.trimIndent()
        )

        generateCommonLayers(
            baseDirectory = baseDirectory,
            packageName = packageName,
            classPrefix = classPrefix,
            di = di,
            architecture = Architecture.MVI,
            networking = networking

        )
    }

    private fun generateEmptyRemoteDataSource(
        packageName: String,
        classPrefix: String
    ): String {
        return """
        package $packageName.data

        class ${classPrefix}RemoteDataSource {

            suspend fun load() {
                // TODO: Implement remote request
            }
        }
    """.trimIndent()
    }

    private fun generateKtorRemoteDataSource(
        packageName: String,
        classPrefix: String
    ): String {
        return """
        package $packageName.data

        import io.ktor.client.HttpClient
        import io.ktor.client.call.body
        import io.ktor.client.request.get

        class ${classPrefix}RemoteDataSource(
            private val httpClient: HttpClient
        ) {

            suspend fun load() {
                // TODO: Replace endpoint with real API URL
                httpClient.get("https://example.com/api/${classPrefix.replaceFirstChar { it.lowercase() }}").body<Unit>()
            }
        }
    """.trimIndent()
    }

    private fun generateMvvmFeature(
        baseDirectory: File,
        packageName: String,
        classPrefix: String,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}UiState.kt"),
            content = """
                package $packageName.presentation

                data class ${classPrefix}UiState(
                    val isLoading: Boolean = false,
                    val error: String? = null
                )
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "presentation/${classPrefix}ViewModel.kt"),
            content = """
                package $packageName.presentation

                import $packageName.domain.${classPrefix}UseCase
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.Dispatchers
                import kotlinx.coroutines.SupervisorJob
                import kotlinx.coroutines.cancel
                import kotlinx.coroutines.flow.MutableStateFlow
                import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asStateFlow
                import kotlinx.coroutines.flow.update
                import kotlinx.coroutines.launch

                class ${classPrefix}ViewModel(
                    private val useCase: ${classPrefix}UseCase
                ) {

                    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

                    private val _uiState = MutableStateFlow(${classPrefix}UiState())
                    val uiState: StateFlow<${classPrefix}UiState> = _uiState.asStateFlow()

                    fun load() {
                        viewModelScope.launch {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = true,
                                    error = null
                                )
                            }

                            try {
                                useCase()

                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isLoading = false,
                                        error = null
                                    )
                                }
                            } catch (exception: Exception) {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        isLoading = false,
                                        error = exception.message ?: "Unknown error"
                                    )
                                }
                            }
                        }
                    }

                    fun retry() {
                        load()
                    }

                    fun clear() {
                        viewModelScope.cancel()
                    }
                }
            """.trimIndent()
        )

        generateCommonLayers(
            baseDirectory = baseDirectory,
            packageName = packageName,
            classPrefix = classPrefix,
            di = di,
            architecture = Architecture.MVVM,
            networking = networking
        )
    }

    private fun generateCommonLayers(
        baseDirectory: File,
        packageName: String,
        classPrefix: String,
        di: Di,
        architecture: Architecture,
        networking: Networking
    ) {
        writeFile(
            file = File(baseDirectory, "domain/${classPrefix}Repository.kt"),
            content = """
                package $packageName.domain

                interface ${classPrefix}Repository {
                    suspend fun load()
                }
            """.trimIndent()
        )

        writeFile(
            file = File(baseDirectory, "domain/${classPrefix}UseCase.kt"),
            content = """
                package $packageName.domain

                class ${classPrefix}UseCase(
                    private val repository: ${classPrefix}Repository
                ) {
                    suspend operator fun invoke() {
                        repository.load()
                    }
                }
            """.trimIndent()
        )

        val remoteDataSourceContent = when (networking) {
            Networking.NONE -> generateEmptyRemoteDataSource(packageName, classPrefix)
            Networking.KTOR -> generateKtorRemoteDataSource(packageName, classPrefix)
        }

        writeFile(
            file = File(baseDirectory, "data/${classPrefix}RemoteDataSource.kt"),
            content = remoteDataSourceContent
        )

        writeFile(
            file = File(baseDirectory, "data/${classPrefix}RepositoryImpl.kt"),
            content = """
                package $packageName.data

                import $packageName.domain.${classPrefix}Repository

                class ${classPrefix}RepositoryImpl(
                    private val remoteDataSource: ${classPrefix}RemoteDataSource
                ) : ${classPrefix}Repository {

                    override suspend fun load() {
                        remoteDataSource.load()
                    }
                }
            """.trimIndent()
        )

        val moduleContent = when (di) {
            Di.NONE -> generateEmptyModule(
                packageName = packageName,
                classPrefix = classPrefix
            )

            Di.KOIN -> generateKoinModule(
                packageName = packageName,
                classPrefix = classPrefix,
                architecture = architecture,
                networking = networking
            )
        }

        writeFile(
            file = File(baseDirectory, "di/${classPrefix}Module.kt"),
            content = moduleContent
        )
    }

    private fun generateEmptyModule(
        packageName: String,
        classPrefix: String
    ): String {
        return """
            package $packageName.di

            // TODO: Add DI module here.
            // Later this file can generate Koin module for ${classPrefix} feature.
        """.trimIndent()
    }

    private fun generateKoinModule(
        packageName: String,
        classPrefix: String,
        architecture: Architecture,
        networking: Networking
    ): String {
        val moduleName = classPrefix.replaceFirstChar { char ->
            char.lowercase()
        } + "Module"

        val reducerImport = when (architecture) {
            Architecture.MVI -> "import $packageName.presentation.${classPrefix}Reducer"
            Architecture.MVVM -> ""
        }

        val reducerFactory = when (architecture) {
            Architecture.MVI -> "factory { ${classPrefix}Reducer() }"
            Architecture.MVVM -> ""
        }

        val viewModelFactory = when (architecture) {
            Architecture.MVI -> "factory { ${classPrefix}ViewModel(get(), get()) }"
            Architecture.MVVM -> "factory { ${classPrefix}ViewModel(get()) }"
        }

        val remoteDataSourceFactory = when (networking) {
            Networking.NONE -> "single { ${classPrefix}RemoteDataSource() }"
            Networking.KTOR -> "single { ${classPrefix}RemoteDataSource(get()) }"
        }

        return """
            package $packageName.di

            import $packageName.data.${classPrefix}RemoteDataSource
            import $packageName.data.${classPrefix}RepositoryImpl
            import $packageName.domain.${classPrefix}Repository
            import $packageName.domain.${classPrefix}UseCase
            import $packageName.presentation.${classPrefix}ViewModel
            $reducerImport
            import org.koin.dsl.module

            val $moduleName = module {
                $remoteDataSourceFactory
                single<${classPrefix}Repository> { ${classPrefix}RepositoryImpl(get()) }
                factory { ${classPrefix}UseCase(get()) }
                $reducerFactory
                $viewModelFactory
            }
        """.trimIndent()
    }

    private fun writeFile(file: File, content: String) {
        if (file.exists()) {
            logger.lifecycle("Skipped existing file: ${file.absolutePath}")
            return
        }

        file.parentFile.mkdirs()
        file.writeText(content)
        logger.lifecycle("Created file: ${file.absolutePath}")
    }
}