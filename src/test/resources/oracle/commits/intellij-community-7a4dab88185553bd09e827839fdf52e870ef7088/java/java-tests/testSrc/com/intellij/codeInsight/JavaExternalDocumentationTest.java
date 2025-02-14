/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight;

import com.intellij.JavaTestUtil;
import com.intellij.codeInsight.documentation.DocumentationComponent;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaExternalDocumentationTest extends PlatformTestCase {

  public static final Pattern BASE_URL_PATTERN = Pattern.compile("(<base href=\")([^\"]*)");
  public static final Pattern IMG_URL_PATTERN = Pattern.compile("<img src=\"([^\"]*)");

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final VirtualFile libClasses = getJarFile("library.jar");
    final VirtualFile libJavadocJar = getJarFile("library-javadoc.jar");

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        final Library library = LibraryTablesRegistrar.getInstance().getLibraryTable(myProject).createLibrary("myLib");
        final Library.ModifiableModel model = library.getModifiableModel();
        model.addRoot(libClasses, OrderRootType.CLASSES);
        model.addRoot(libJavadocJar, JavadocOrderRootType.getInstance());
        model.commit();

        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        assertSize(1, modules);
        ModuleRootModificationUtil.addDependency(modules[0], library);
      }
    });
  }

  public void testImagesInsideJavadocJar() throws Exception {
    String text = getDocumentationText("class Foo { com.jetbrains.<caret>Test field; }");
    Matcher baseUrlmatcher = BASE_URL_PATTERN.matcher(text);
    assertTrue(baseUrlmatcher.find());
    String baseUrl = baseUrlmatcher.group(2);
    Matcher imgMatcher = IMG_URL_PATTERN.matcher(text);
    assertTrue(imgMatcher.find());
    String relativeUrl = imgMatcher.group(1);
    URL imageUrl = new URL(new URL(baseUrl), relativeUrl);
    InputStream stream = imageUrl.openStream();
    try {
      assertEquals(228, FileUtil.loadBytes(stream).length);
    }
    finally {
      stream.close();
    }
  }
  
  // We're guessing style of references in javadoc by bytecode version of library class file
  // but displaying quick doc should work even if javadoc was generated using a JDK not corresponding to bytecode version
  public void testReferenceStyleDoesntMatchBytecodeVersion() throws Exception {
    String actualText = getDocumentationText("@com.jetbrains.TestAnnotation(<caret>param = \"foo\") class Foo {}");
    String expectedText = FileUtil.loadFile(getDataFile(getTestName(false) + ".html"));
    assertEquals(expectedText, replaceBaseUrlWithPlaceholder(actualText));
  }

  private static String replaceBaseUrlWithPlaceholder(String actualText) {
    return BASE_URL_PATTERN.matcher(actualText).replaceAll("$1placeholder");
  }

  private static void waitTillDone(ActionCallback actionCallback) throws InterruptedException {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 300000) {
      //noinspection BusyWait
      Thread.sleep(100);
      UIUtil.dispatchAllInvocationEvents();
      if (actionCallback.isProcessed()) return;
    }
    fail("Timed out waiting for documentation to show");
  }

  private static File getDataFile(String name) {
    return new File(JavaTestUtil.getJavaTestDataPath() + "/codeInsight/documentation/" + name);
  }
  
  @NotNull
  private static VirtualFile getJarFile(String name) {
    VirtualFile file = getVirtualFile(getDataFile(name));
    assertNotNull(file);
    VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(file);
    assertNotNull(jarFile);
    return jarFile;
  }

  private String getDocumentationText(String sourceEditorText) throws Exception {
    int caretPosition = sourceEditorText.indexOf(EditorTestUtil.CARET_TAG);
    if (caretPosition >= 0) {
      sourceEditorText = sourceEditorText.substring(0, caretPosition) + 
                         sourceEditorText.substring(caretPosition + EditorTestUtil.CARET_TAG.length());
    }
    PsiFile psiFile = PsiFileFactory.getInstance(myProject).createFileFromText(JavaLanguage.INSTANCE, sourceEditorText);
    Document document = PsiDocumentManager.getInstance(myProject).getDocument(psiFile);
    assertNotNull(document);
    Editor editor = EditorFactory.getInstance().createEditor(document, myProject);
    try {
      if (caretPosition >= 0) {
        editor.getCaretModel().moveToOffset(caretPosition);
      }
      DocumentationManager documentationManager = DocumentationManager.getInstance(myProject);
      MockDocumentationComponent documentationComponent = new MockDocumentationComponent(documentationManager);
      try {
        documentationManager.setDocumentationComponent(documentationComponent);
        documentationManager.showJavaDocInfo(editor, psiFile, false);
        waitTillDone(documentationManager.getLastAction());
        return documentationComponent.getText();
      }
      finally {
        Disposer.dispose(documentationComponent);
      }
    }
    finally {
      EditorFactory.getInstance().releaseEditor(editor);
    }
  }

  @Override
  protected boolean isRunInWriteAction() {
    return false;
  }
  
  private static class MockDocumentationComponent extends DocumentationComponent {
    private String myText;
    
    public MockDocumentationComponent(DocumentationManager manager) {
      super(manager);
    }

    @Override
    public void setText(String text, PsiElement element, boolean clean, boolean clearHistory) {
      myText = text;
    }

    @Override
    public void setData(PsiElement _element, String text, boolean clearHistory, String effectiveExternalUrl) {
      myText = text;
    }

    public String getText() {
      return myText;
    }
  }
}
