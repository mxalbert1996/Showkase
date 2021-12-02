package com.airbnb.android.showkase.processor.writer

import androidx.room.compiler.processing.*
import com.airbnb.android.showkase.processor.writer.ShowkaseBrowserWriter.Companion.SHOWKASE_MODELS_PACKAGE_NAME
import com.airbnb.android.showkase.processor.writer.ShowkaseBrowserWriter.Companion.SHOWKASE_PROVIDER_CLASS_NAME
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import javax.annotation.processing.ProcessingEnvironment

internal class ShowkaseExtensionFunctionsWriter(
    private val environment: XProcessingEnv
) {
    internal fun generateShowkaseExtensionFunctions(
        rootModulePackageName: String,
        rootModuleClassName: String,
        rootElement: XTypeElement
    ) {
        getFileBuilder(
            rootModulePackageName,
            "${rootModuleClassName}$SHOWKASE_METHODS_SUFFIX"
        )
            .addFunction(
                generateIntentFunction(
                    rootModulePackageName,
                    rootModuleClassName,
                    rootElement
                )
            )
            .addFunction(
                generateMetadataFunction(
                    rootElement,
                    "$rootModulePackageName.$rootModuleClassName"
                )
            )
            .build()
            .writeTo(environment.filer, mode = XFiler.Mode.Aggregating)
    }

    private fun generateIntentFunction(
        rootModulePackageName: String,
        rootModuleClassName: String,
        rootElement: XTypeElement,
    ) = FunSpec.builder(INTENT_FUNCTION_NAME).apply {
        addParameter(
            CONTEXT_PARAMETER_NAME, CONTEXT_CLASS_NAME
        )
        addKdoc("Helper function that's autogenerated and gives you an intent to start the " +
                "ShowkaseBrowser.")
        receiver(SHOWKASE_OBJECT_CLASS_NAME)
        returns(INTENT_CLASS_NAME)
        addCode(
            CodeBlock.Builder()
                .indent()
                .addStatement(
                    "val intent = %T(%L, %T::class.java)",
                    INTENT_CLASS_NAME,
                    CONTEXT_PARAMETER_NAME,
                    SHOWKASE_BROWSER_ACTIVITY_CLASS_NAME
                )
                .addStatement(
                    "intent.putExtra(%S, %S)",
                    SHOWKASE_ROOT_MODULE_KEY,
                    "$rootModulePackageName.$rootModuleClassName"
                )
                .addStatement(
                    "return intent"
                )
                .unindent()
                .build()
        )
        addOriginatingElement(rootElement)
    }
        .build()

    private fun generateMetadataFunction(
        rootElement: XTypeElement,
        classKey: String
    ) = FunSpec.builder(METADATA_FUNCTION_NAME).apply {
        val errorMessage = "The class wasn't generated correctly. Make sure that you have setup " +
                "Showkase correctly by following the steps here - " +
                "https://github.com/airbnb/Showkase#Installation."
        receiver(SHOWKASE_OBJECT_CLASS_NAME)
        returns(SHOWKASE_ELEMENTS_METADATA_CLASS_NAME)
        addKdoc("Helper function that's give's you access to Showkase metadata. This contains " +
                "data about the composables, colors and typography in your codebase that's " +
                "rendered in showakse.")
        addCode(
            CodeBlock.Builder()
                .indent()
                .addStatement("try {")
                .indent()
                .addStatement(
                    "val showkaseComponentProvider = Class.forName(\"${classKey}Codegen\").newInstance() as %T",
                    SHOWKASE_PROVIDER_CLASS_NAME
                )
                .addStatement("return %L.metadata()", "showkaseComponentProvider")
                .unindent()
                .addStatement("} catch(exception: ClassNotFoundException) {")
                .indent()
                .addStatement("error(%S)", errorMessage)
                .unindent()
                .addStatement("}")
                .unindent()
                .build()
        )
        addOriginatingElement(rootElement)
    }
        .build()

    companion object {
        private const val SHOWKASE_ROOT_MODULE_KEY = "SHOWKASE_ROOT_MODULE"
        private const val INTENT_FUNCTION_NAME = "getBrowserIntent"
        private const val METADATA_FUNCTION_NAME = "getMetadata"
        private const val SHOWKASE_EXTENSION_FUNCTIONS_NAME = "ShowkaseExtensionFunctions"
        private const val SHOWKASE_METHODS_SUFFIX = "${SHOWKASE_EXTENSION_FUNCTIONS_NAME}Codegen"
        private const val CONTEXT_PARAMETER_NAME = "context"
        private const val CONTEXT_PACKAGE_NAME = "android.content"
        internal val CONTEXT_CLASS_NAME =
            ClassName(CONTEXT_PACKAGE_NAME, "Context")
        private val INTENT_CLASS_NAME =
            ClassName(CONTEXT_PACKAGE_NAME, "Intent")
        private val SHOWKASE_BROWSER_ACTIVITY_CLASS_NAME =
            ClassName("com.airbnb.android.showkase.ui", "ShowkaseBrowserActivity")
        private val SHOWKASE_ELEMENTS_METADATA_CLASS_NAME = 
            ClassName(SHOWKASE_MODELS_PACKAGE_NAME, "ShowkaseElementsMetadata")
        internal val SHOWKASE_OBJECT_CLASS_NAME =
            ClassName(SHOWKASE_MODELS_PACKAGE_NAME, "Showkase")
    }
}
