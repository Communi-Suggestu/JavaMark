package com.communi.suggestu.javamark.doclet.utils;

import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EscapeTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.SpecTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.internal.doclets.formats.html.HtmlConfiguration;

import java.util.List;

public class DocTreeUtils
{
    public static List<? extends DocTree> getTags(DocTree dtree, HtmlConfiguration configuration) {
        return new SimpleDocTreeVisitor<List<? extends DocTree>, Void>() {

            private List<DocTree> asList(String content) {
                return List.of(configuration.cmtUtils.makeTextTree(content));
            }

            @Override
            public List<? extends DocTree> visitAuthor(AuthorTree node, Void p) {
                return node.getName();
            }

            @Override
            public List<? extends DocTree> visitComment(CommentTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitDeprecated(DeprecatedTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitDocComment(DocCommentTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitEscape(EscapeTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitLiteral(LiteralTree node, Void p) {
                return asList(node.getBody().getBody());
            }

            @Override
            public List<? extends DocTree> visitProvides(ProvidesTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSince(SinceTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitText(TextTree node, Void p) {
                return asList(node.getBody());
            }

            @Override
            public List<? extends DocTree> visitVersion(VersionTree node, Void p) {
                return node.getBody();
            }

            @Override
            public List<? extends DocTree> visitParam(ParamTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitReturn(ReturnTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSee(SeeTree node, Void p) {
                return node.getReference();
            }

            @Override
            public List<? extends DocTree> visitSerial(SerialTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSerialData(SerialDataTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSerialField(SerialFieldTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitSpec(SpecTree node, Void p) {
                return node.getTitle();
            }

            @Override
            public List<? extends DocTree> visitThrows(ThrowsTree node, Void p) {
                return node.getDescription();
            }

            @Override
            public List<? extends DocTree> visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
                return node.getContent();
            }

            @Override
            public List<? extends DocTree> visitUses(UsesTree node, Void p) {
                return node.getDescription();
            }

            @Override
            protected List<? extends DocTree> defaultAction(DocTree node, Void p) {
                return List.of();
            }
        }.visit(dtree, null);
    }
}
