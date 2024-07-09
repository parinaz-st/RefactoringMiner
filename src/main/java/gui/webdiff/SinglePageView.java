package gui.webdiff;

import org.rendersnake.DocType;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import static org.rendersnake.HtmlAttributesFactory.*;

/* Created by pourya on 2024-07-04*/
public class SinglePageView extends DirectoryDiffView implements Renderable {
    private final String JQ_UI_CSS = "https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css";
    private final String JQ_UI_JS = "https://code.jquery.com/ui/1.12.1/jquery-ui.js";
    public SinglePageView(DirComparator comparator) {
        super(comparator);
    }

    @Override
    public void renderOn(HtmlCanvas html) {
        int n = comparator.getNumOfDiffs();
        try {
            html
                    .head()
                    .meta(charset("utf8"))
                    .meta(name("viewport").content("width=device-width, initial-scale=1.0"))
                    .macros().stylesheet("/dist/single.css")
                    .macros().stylesheet(JQ_UI_CSS)
                    ._head()
                    .render(DocType.HTML5)
                    .html(lang("en"))
                    .body()
                    .div(class_("container-fluid"))
                    .div(class_("row h-100"))
                    .div(class_("col-2 bg-light dir-diff"))
                    .render(new DirectoryDiffView(comparator, true))
                    ._div()
                    // Monaco editors 4/5 width
                    .div(class_("col-10 monaco-panel"))
                    .div(id("accordion"));

            // Generate panels for /monaco-0 to /monaco-n
            for (int i = 0; i < n; i++) {
                MonacoDiffViewCore core = new MonacoDiffViewCore(comparator.getASTDiff(i), i, false);
                html.div(class_("card"))
                        .div(class_("card-header").id("heading-" + i).style("padding-right: 0;"))
                        .div(class_("d-flex align-items-center justify-content-between"))
                        .div(class_("flex-grow-1").style("word-break: break-word; overflow-wrap: anywhere;"))
                        .h5(class_("mb-0"))
                            .a(class_("")
                                .add("data-bs-toggle", "collapse")
                                .add("data-bs-target", "#collapse-" + i)
                                .add("aria-expanded", "true")
                                .add("aria-controls", "collapse-" + i)).content(core.getDiffName())
                        ._h5()
                        ._div()
                        .div(class_("text-end"))
                        .a(href("monaco-page/" + i).class_("btn btn-primary btn sm")).content("Details")
                        ._div()
                        ._div()
                        .div(id("collapse-" + i).class_("collapse show").add("aria-labelledby", "heading-" + i))
                        .div(class_("card-body").style("padding: 0;"))
                        .iframe(src("/monaco-diff/" + i)
                                .id("monaco-diff-" + i)
                                .style("width: 100%; height: 500px; border: none;"))
                        ._iframe()
                        ._div()
                        ._div()
                        ._div()
                        ._div();

            }

            html._div() // Close accordion div
                    ._div() // Close monaco-panel div
                    ._div() // Close row div
                    ._div(); // Close container-fluid div


            html.
                    macros().javascript("/dist/single.js")
                    .macros().javascript(JQ_UI_JS)
                    ._body()
                    ._html();

        } catch (Exception e) {
            System.out.println(e);
            // Handle exception
        }

    }
}