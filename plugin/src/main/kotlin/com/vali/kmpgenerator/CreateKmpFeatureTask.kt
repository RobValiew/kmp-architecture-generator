package com.vali.kmpgenerator

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class CreateKmpFeatureTask : DefaultTask() {

    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    @get:Input
    abstract val defaultPackageName: Property<String>

    @get:Input
    abstract val defaultOutputDir: Property<String>

    @get:Input
    abstract val defaultArchitecture: Property<String>

    @get:Input
    abstract val defaultDi: Property<String>

    @get:Input
    abstract val defaultNetworking: Property<String>

    @get:Input
    @get:Optional
    abstract val featureNameOption: Property<String>

    @get:Input
    @get:Optional
    abstract val architectureOption: Property<String>

    @get:Input
    @get:Optional
    abstract val packageNameOption: Property<String>

    @get:Input
    @get:Optional
    abstract val outputDirOption: Property<String>

    @get:Input
    @get:Optional
    abstract val diOption: Property<String>

    @get:Input
    @get:Optional
    abstract val networkingOption: Property<String>

    @get:Input
    abstract val recreateOption: Property<Boolean>

    init {
        recreateOption.convention(false)
    }

    @Option(option = "name", description = "Feature name")
    fun setFeatureName(value: String) {
        featureNameOption.set(value)
    }

    @Option(option = "architecture", description = "Architecture type: mvi or mvvm")
    fun setArchitecture(value: String) {
        architectureOption.set(value)
    }

    @Option(option = "packageName", description = "Base package name")
    fun setPackageName(value: String) {
        packageNameOption.set(value)
    }

    @Option(option = "outputDir", description = "Output directory")
    fun setOutputDir(value: String) {
        outputDirOption.set(value)
    }

    @Option(option = "di", description = "DI type: none or koin")
    fun setDi(value: String) {
        diOption.set(value)
    }

    @Option(option = "networking", description = "Networking type: none or ktor")
    fun setNetworking(value: String) {
        networkingOption.set(value)
    }

    @Option(option = "recreate", description = "Recreate existing generated feature")
    fun setRecreate(value: Boolean) {
        recreateOption.set(value)
    }

    @TaskAction
    fun generate() {
        val featureName = featureNameOption.orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: error("Missing required option: --name")

        val architecture = Architecture.from(
            architectureOption.orNull ?: defaultArchitecture.get()
        )

        val packageName = packageNameOption.orNull ?: defaultPackageName.get()
        val outputDir = outputDirOption.orNull ?: defaultOutputDir.get()

        val di = Di.from(
            diOption.orNull ?: defaultDi.get()
        )

        val networking = Networking.from(
            networkingOption.orNull ?: defaultNetworking.get()
        )

        val recreate = recreateOption.get()

        val classPrefix = featureName.toPascalCase()
        val featureDirectoryName = featureName.toPackageSegment()
        val packagePath = packageName.replace(".", File.separator)

        val outputRoot = projectDirectory
            .get()
            .asFile
            .resolve(outputDir)
            .resolve(packagePath)

        val featureDir = outputRoot
            .resolve("features")
            .resolve(featureDirectoryName)

        val featurePackage = "$packageName.features.$featureDirectoryName"

        recreateFeatureDirectoryIfNeeded(
            featureName = featureName,
            featureDir = featureDir,
            architecture = architecture,
            recreate = recreate
        )

        when (architecture) {
            Architecture.MVI -> generateMviFeature(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix,
                di = di,
                networking = networking
            )

            Architecture.MVVM -> generateMvvmFeature(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix,
                di = di,
                networking = networking
            )
        }

        generateCommonLayers(
            featureDir = featureDir,
            featurePackage = featurePackage,
            classPrefix = classPrefix,
            di = di,
            networking = networking
        )

        generateNetworkCoreIfNeeded(
            outputRoot = outputRoot,
            packageName = packageName,
            di = di,
            networking = networking
        )

        writeMarkerFile(
            featureDir = featureDir,
            featureName = featureName,
            architecture = architecture,
            di = di,
            networking = networking
        )

        logger.lifecycle("KMP feature '$featureName' was generated successfully.")
    }

    private fun recreateFeatureDirectoryIfNeeded(
        featureName: String,
        featureDir: File,
        architecture: Architecture,
        recreate: Boolean
    ) {
        if (!featureDir.exists()) {
            return
        }

        val markerFile = featureDir.resolve(MARKER_FILE_NAME)

        if (!markerFile.exists()) {
            error(
                "Cannot recreate feature '$featureName'. Directory exists, but marker file was not found. " +
                        "This directory may contain manually written code. Delete it manually if you really want to recreate it."
            )
        }

        if (!recreate) {
            val markerText = markerFile.readText()
            val previousArchitecture = markerText
                .lineSequence()
                .firstOrNull { line -> line.startsWith("architecture=") }
                ?.substringAfter("architecture=")

            if (previousArchitecture != null && previousArchitecture != architecture.name) {
                error(
                    "Feature '$featureName' already exists with architecture '$previousArchitecture'. " +
                            "Requested architecture is '${architecture.name}'. Use --recreate to recreate it."
                )
            }

            error(
                "Feature '$featureName' already exists. Use --recreate to recreate it."
            )
        }

        featureDir.deleteRecursively()
    }

    private fun writeMarkerFile(
        featureDir: File,
        featureName: String,
        architecture: Architecture,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = featureDir.resolve(MARKER_FILE_NAME),
            content = """
                name=$featureName
                architecture=${architecture.name}
                di=${di.name}
                networking=${networking.name}
            """.trimIndent(),
            overwrite = true
        )
    }

    private fun generateMviFeature(
        featureDir: File,
        featurePackage: String,
        classPrefix: String,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}State.kt"),
            content = """
                package $featurePackage.presentation

                data class ${classPrefix}State(
                    val isLoading: Boolean = false,
                    val errorMessage: String? = null
                )
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}Action.kt"),
            content = """
                package $featurePackage.presentation

                sealed interface ${classPrefix}Action {
                    data object Load : ${classPrefix}Action
                    data object Retry : ${classPrefix}Action
                }
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}Result.kt"),
            content = """
                package $featurePackage.presentation

                sealed interface ${classPrefix}Result {
                    data object Loading : ${classPrefix}Result
                    data object Success : ${classPrefix}Result
                    data class Error(val message: String) : ${classPrefix}Result
                }
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}Effect.kt"),
            content = """
                package $featurePackage.presentation

                sealed interface ${classPrefix}Effect {
                    data class ShowMessage(val message: String) : ${classPrefix}Effect
                }
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}Reducer.kt"),
            content = """
                package $featurePackage.presentation

                class ${classPrefix}Reducer {

                    fun reduce(
                        state: ${classPrefix}State,
                        result: ${classPrefix}Result
                    ): ${classPrefix}State {
                        return when (result) {
                            ${classPrefix}Result.Loading -> state.copy(
                                isLoading = true,
                                errorMessage = null
                            )

                            ${classPrefix}Result.Success -> state.copy(
                                isLoading = false,
                                errorMessage = null
                            )

                            is ${classPrefix}Result.Error -> state.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}ViewModel.kt"),
            content = """
                package $featurePackage.presentation

                import $featurePackage.domain.${classPrefix}UseCase
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.Dispatchers
                import kotlinx.coroutines.SupervisorJob
                import kotlinx.coroutines.flow.MutableSharedFlow
                import kotlinx.coroutines.flow.MutableStateFlow
                import kotlinx.coroutines.flow.SharedFlow
                import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asSharedFlow
                import kotlinx.coroutines.flow.asStateFlow
                import kotlinx.coroutines.launch

                class ${classPrefix}ViewModel(
                    private val useCase: ${classPrefix}UseCase,
                    private val reducer: ${classPrefix}Reducer
                ) {

                    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

                    private val _state = MutableStateFlow(${classPrefix}State())
                    val state: StateFlow<${classPrefix}State> = _state.asStateFlow()

                    private val _effect = MutableSharedFlow<${classPrefix}Effect>()
                    val effect: SharedFlow<${classPrefix}Effect> = _effect.asSharedFlow()

                    fun onAction(action: ${classPrefix}Action) {
                        when (action) {
                            ${classPrefix}Action.Load,
                            ${classPrefix}Action.Retry -> load()
                        }
                    }

                    private fun load() {
                        viewModelScope.launch {
                            reduce(${classPrefix}Result.Loading)

                            runCatching {
                                useCase()
                            }.onSuccess {
                                reduce(${classPrefix}Result.Success)
                            }.onFailure { throwable ->
                                val message = throwable.message ?: "Unknown error"
                                reduce(${classPrefix}Result.Error(message))
                                _effect.emit(${classPrefix}Effect.ShowMessage(message))
                            }
                        }
                    }

                    private fun reduce(result: ${classPrefix}Result) {
                        _state.value = reducer.reduce(
                            state = _state.value,
                            result = result
                        )
                    }
                }
            """.trimIndent()
        )
    }

    private fun generateMvvmFeature(
        featureDir: File,
        featurePackage: String,
        classPrefix: String,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}UiState.kt"),
            content = """
                package $featurePackage.presentation

                data class ${classPrefix}UiState(
                    val isLoading: Boolean = false,
                    val errorMessage: String? = null
                )
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("presentation/${classPrefix}ViewModel.kt"),
            content = """
                package $featurePackage.presentation

                import $featurePackage.domain.${classPrefix}UseCase
                import kotlinx.coroutines.CoroutineScope
                import kotlinx.coroutines.Dispatchers
                import kotlinx.coroutines.SupervisorJob
                import kotlinx.coroutines.flow.MutableStateFlow
                import kotlinx.coroutines.flow.StateFlow
                import kotlinx.coroutines.flow.asStateFlow
                import kotlinx.coroutines.launch

                class ${classPrefix}ViewModel(
                    private val useCase: ${classPrefix}UseCase
                ) {

                    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

                    private val _uiState = MutableStateFlow(${classPrefix}UiState())
                    val uiState: StateFlow<${classPrefix}UiState> = _uiState.asStateFlow()

                    fun load() {
                        viewModelScope.launch {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                errorMessage = null
                            )

                            runCatching {
                                useCase()
                            }.onSuccess {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }.onFailure { throwable ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = throwable.message ?: "Unknown error"
                                )
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    private fun generateCommonLayers(
        featureDir: File,
        featurePackage: String,
        classPrefix: String,
        di: Di,
        networking: Networking
    ) {
        writeFile(
            file = featureDir.resolve("domain/${classPrefix}Repository.kt"),
            content = """
                package $featurePackage.domain

                interface ${classPrefix}Repository {
                    suspend fun load()
                }
            """.trimIndent()
        )

        writeFile(
            file = featureDir.resolve("domain/${classPrefix}UseCase.kt"),
            content = """
                package $featurePackage.domain

                class ${classPrefix}UseCase(
                    private val repository: ${classPrefix}Repository
                ) {

                    suspend operator fun invoke() {
                        repository.load()
                    }
                }
            """.trimIndent()
        )

        when (networking) {
            Networking.NONE -> generateEmptyRemoteDataSource(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix
            )

            Networking.KTOR -> generateKtorRemoteDataSource(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix
            )
        }

        writeFile(
            file = featureDir.resolve("data/${classPrefix}RepositoryImpl.kt"),
            content = """
                package $featurePackage.data

                import $featurePackage.domain.${classPrefix}Repository

                class ${classPrefix}RepositoryImpl(
                    private val remoteDataSource: ${classPrefix}RemoteDataSource
                ) : ${classPrefix}Repository {

                    override suspend fun load() {
                        remoteDataSource.load()
                    }
                }
            """.trimIndent()
        )

        when (di) {
            Di.NONE -> generateEmptyModule(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix
            )

            Di.KOIN -> generateKoinModule(
                featureDir = featureDir,
                featurePackage = featurePackage,
                classPrefix = classPrefix,
                networking = networking
            )
        }
    }

    private fun generateEmptyRemoteDataSource(
        featureDir: File,
        featurePackage: String,
        classPrefix: String
    ) {
        writeFile(
            file = featureDir.resolve("data/${classPrefix}RemoteDataSource.kt"),
            content = """
                package $featurePackage.data

                class ${classPrefix}RemoteDataSource {

                    suspend fun load() {
                        // TODO: Implement remote data loading
                    }
                }
            """.trimIndent()
        )
    }

    private fun generateKtorRemoteDataSource(
        featureDir: File,
        featurePackage: String,
        classPrefix: String
    ) {
        val endpointName = classPrefix.replaceFirstChar { char ->
            char.lowercase()
        }

        writeFile(
            file = featureDir.resolve("data/${classPrefix}RemoteDataSource.kt"),
            content = """
                package $featurePackage.data

                import io.ktor.client.HttpClient
                import io.ktor.client.call.body
                import io.ktor.client.request.get

                class ${classPrefix}RemoteDataSource(
                    private val httpClient: HttpClient
                ) {

                    suspend fun load() {
                        httpClient.get("https://example.com/api/$endpointName").body<Unit>()
                    }
                }
            """.trimIndent()
        )
    }

    private fun generateEmptyModule(
        featureDir: File,
        featurePackage: String,
        classPrefix: String
    ) {
        writeFile(
            file = featureDir.resolve("di/${classPrefix}Module.kt"),
            content = """
                package $featurePackage.di

                // Dependency injection is disabled for this feature.
                // Create your platform-specific or manual dependencies here if needed.
            """.trimIndent()
        )
    }

    private fun generateKoinModule(
        featureDir: File,
        featurePackage: String,
        classPrefix: String,
        networking: Networking
    ) {
        val moduleName = classPrefix.replaceFirstChar { char ->
            char.lowercase()
        }

        val remoteDataSourceFactory = when (networking) {
            Networking.NONE -> "single { ${classPrefix}RemoteDataSource() }"
            Networking.KTOR -> "single { ${classPrefix}RemoteDataSource(get()) }"
        }

        val reducerFactory = if (featureDir.resolve("presentation/${classPrefix}Reducer.kt").exists()) {
            "    factory { ${classPrefix}Reducer() }\n"
        } else {
            ""
        }

        val viewModelFactory = if (reducerFactory.isNotEmpty()) {
            "    factory { ${classPrefix}ViewModel(get(), get()) }"
        } else {
            "    factory { ${classPrefix}ViewModel(get()) }"
        }

        val reducerImport = if (reducerFactory.isNotEmpty()) {
            "import $featurePackage.presentation.${classPrefix}Reducer\n"
        } else {
            ""
        }

        writeFile(
            file = featureDir.resolve("di/${classPrefix}Module.kt"),
            content = """
                package $featurePackage.di

                import $featurePackage.data.${classPrefix}RemoteDataSource
                import $featurePackage.data.${classPrefix}RepositoryImpl
                import $featurePackage.domain.${classPrefix}Repository
                import $featurePackage.domain.${classPrefix}UseCase
                ${reducerImport}import $featurePackage.presentation.${classPrefix}ViewModel
                import org.koin.dsl.module

                val ${moduleName}Module = module {
                    $remoteDataSourceFactory
                    single<${classPrefix}Repository> { ${classPrefix}RepositoryImpl(get()) }
                    factory { ${classPrefix}UseCase(get()) }
                $reducerFactory    $viewModelFactory
                }
            """.trimIndent()
        )
    }

    private fun generateNetworkCoreIfNeeded(
        outputRoot: File,
        packageName: String,
        di: Di,
        networking: Networking
    ) {
        if (networking != Networking.KTOR) {
            return
        }

        val networkDir = outputRoot.resolve("core/network")
        val networkPackage = "$packageName.core.network"

        generateHttpClientFactory(
            networkDir = networkDir,
            networkPackage = networkPackage
        )

        if (di == Di.KOIN) {
            generateNetworkModule(
                networkDir = networkDir,
                networkPackage = networkPackage
            )
        }
    }

    private fun generateHttpClientFactory(
        networkDir: File,
        networkPackage: String
    ) {
        writeFile(
            file = networkDir.resolve("HttpClientFactory.kt"),
            content = """
                package $networkPackage

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
            """.trimIndent(),
            overwrite = false
        )
    }

    private fun generateNetworkModule(
        networkDir: File,
        networkPackage: String
    ) {
        writeFile(
            file = networkDir.resolve("NetworkModule.kt"),
            content = """
                package $networkPackage

                import org.koin.dsl.module

                val networkModule = module {
                    single {
                        HttpClientFactory.create()
                    }
                }
            """.trimIndent(),
            overwrite = false
        )
    }

    private fun writeFile(
        file: File,
        content: String,
        overwrite: Boolean = true
    ) {
        if (file.exists() && !overwrite) {
            logger.lifecycle("File already exists, skipping: ${file.path}")
            return
        }

        file.parentFile.mkdirs()
        file.writeText(content)
        logger.lifecycle("Generated file: ${file.path}")
    }

    private companion object {
        const val MARKER_FILE_NAME = ".kmp-feature-generator"
    }
}