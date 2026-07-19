from __future__ import annotations

import re
import sys
import textwrap
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    CondPageBreak,
    KeepTogether,
    LongTable,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
    XPreformatted,
)


PAGE_WIDTH, PAGE_HEIGHT = A4
LEFT = 22 * mm
RIGHT = 22 * mm
TOP = 20 * mm
BOTTOM = 15 * mm
CONTENT_WIDTH = PAGE_WIDTH - LEFT - RIGHT


def register_fonts() -> None:
    fonts = Path("C:/Windows/Fonts")
    candidates = {
        "DocRegular": fonts / "arial.ttf",
        "DocBold": fonts / "arialbd.ttf",
        "DocItalic": fonts / "ariali.ttf",
        "DocMono": fonts / "consola.ttf",
    }
    for name, path in candidates.items():
        if not path.exists():
            raise FileNotFoundError(f"Fuente no encontrada: {path}")
        pdfmetrics.registerFont(TTFont(name, str(path)))


def inline_markup(value: str) -> str:
    value = escape(value.strip())
    value = re.sub(
        r"\[([^\]]+)\]\(([^)]+)\)",
        r'<link href="\2" color="#1565C0"><u>\1</u></link>',
        value,
    )
    value = re.sub(r"`([^`]+)`", r'<font name="DocMono" color="#37474F">\1</font>', value)
    value = re.sub(r"\*\*([^*]+)\*\*", r"<b>\1</b>", value)
    value = re.sub(r"(?<!\*)\*([^*]+)\*(?!\*)", r"<i>\1</i>", value)
    return value


def build_styles():
    base = getSampleStyleSheet()
    styles = {
        "body": ParagraphStyle(
            "Body",
            parent=base["BodyText"],
            fontName="DocRegular",
            fontSize=9.4,
            leading=13.2,
            alignment=TA_JUSTIFY,
            textColor=colors.HexColor("#263238"),
            spaceAfter=5,
        ),
        "h1": ParagraphStyle(
            "H1",
            parent=base["Heading1"],
            fontName="DocBold",
            fontSize=21,
            leading=25,
            alignment=TA_LEFT,
            textColor=colors.HexColor("#0D3B66"),
            spaceBefore=4,
            spaceAfter=12,
        ),
        "h2": ParagraphStyle(
            "H2",
            parent=base["Heading2"],
            fontName="DocBold",
            fontSize=14,
            leading=18,
            textColor=colors.HexColor("#0D3B66"),
            spaceBefore=13,
            spaceAfter=7,
            keepWithNext=True,
        ),
        "h3": ParagraphStyle(
            "H3",
            parent=base["Heading3"],
            fontName="DocBold",
            fontSize=11.3,
            leading=14.5,
            textColor=colors.HexColor("#1565C0"),
            spaceBefore=9,
            spaceAfter=5,
            keepWithNext=True,
        ),
        "bullet": ParagraphStyle(
            "Bullet",
            parent=base["BodyText"],
            fontName="DocRegular",
            fontSize=9.2,
            leading=12.6,
            leftIndent=14,
            firstLineIndent=-8,
            textColor=colors.HexColor("#263238"),
            spaceAfter=1.5,
        ),
        "code": ParagraphStyle(
            "Code",
            parent=base["Code"],
            fontName="DocMono",
            fontSize=7.4,
            leading=9.6,
            leftIndent=7,
            rightIndent=7,
            borderColor=colors.HexColor("#CFD8DC"),
            borderWidth=0.6,
            borderPadding=7,
            backColor=colors.HexColor("#F5F7F8"),
            textColor=colors.HexColor("#263238"),
            spaceBefore=4,
            spaceAfter=8,
        ),
        "table": ParagraphStyle(
            "TableCell",
            parent=base["BodyText"],
            fontName="DocRegular",
            fontSize=6.9,
            leading=8.7,
            textColor=colors.HexColor("#263238"),
        ),
        "table_head": ParagraphStyle(
            "TableHead",
            parent=base["BodyText"],
            fontName="DocBold",
            fontSize=7.1,
            leading=8.8,
            textColor=colors.white,
        ),
        "cover_title": ParagraphStyle(
            "CoverTitle",
            parent=base["Title"],
            fontName="DocBold",
            fontSize=27,
            leading=34,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#0D3B66"),
        ),
        "cover_subtitle": ParagraphStyle(
            "CoverSubtitle",
            parent=base["BodyText"],
            fontName="DocRegular",
            fontSize=12,
            leading=17,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#546E7A"),
        ),
        "cover_meta": ParagraphStyle(
            "CoverMeta",
            parent=base["BodyText"],
            fontName="DocRegular",
            fontSize=10,
            leading=15,
            alignment=TA_CENTER,
            textColor=colors.HexColor("#455A64"),
        ),
    }
    return styles


def split_table_row(line: str) -> list[str]:
    return [cell.strip() for cell in line.strip().strip("|").split("|")]


def is_table_separator(line: str) -> bool:
    cells = split_table_row(line)
    return bool(cells) and all(re.fullmatch(r":?-{3,}:?", cell.replace(" ", "")) for cell in cells)


def make_table(rows: list[list[str]], styles):
    column_count = max(len(row) for row in rows)
    normalized = [row + [""] * (column_count - len(row)) for row in rows]
    data = []
    for row_index, row in enumerate(normalized):
        style = styles["table_head"] if row_index == 0 else styles["table"]
        data.append([Paragraph(inline_markup(cell), style) for cell in row])

    weights = []
    for col in range(column_count):
        longest = max(len(row[col]) for row in normalized)
        weights.append(max(8, min(longest, 34)))
    total = sum(weights)
    widths = [CONTENT_WIDTH * weight / total for weight in weights]

    table = LongTable(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    table.setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#0D3B66")),
                ("GRID", (0, 0), (-1, -1), 0.45, colors.HexColor("#B0BEC5")),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("LEFTPADDING", (0, 0), (-1, -1), 4),
                ("RIGHTPADDING", (0, 0), (-1, -1), 4),
                ("TOPPADDING", (0, 0), (-1, -1), 4),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
                ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F5F7F8")]),
            ]
        )
    )
    return table


def code_block(text: str, styles):
    wrapped_lines: list[str] = []
    for line in text.splitlines() or [""]:
        wrapped = textwrap.wrap(
            line,
            width=92,
            replace_whitespace=False,
            drop_whitespace=False,
            subsequent_indent="  ",
        )
        wrapped_lines.extend(wrapped or [""])
    return XPreformatted(escape("\n".join(wrapped_lines)), styles["code"])


def parse_markdown(text: str, styles):
    lines = text.replace("\r\n", "\n").split("\n")
    story = []
    paragraph: list[str] = []
    in_code = False
    code_lines: list[str] = []
    skipped_title = False
    i = 0

    def flush_paragraph():
        if paragraph:
            paragraph_text = " ".join(part.strip() for part in paragraph)
            # The PDF is standalone, so omit the Markdown-only pointer to a sibling file.
            if not paragraph_text.startswith("La lista ampliada de fuentes está en"):
                story.append(Paragraph(inline_markup(paragraph_text), styles["body"]))
            paragraph.clear()

    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        if stripped.startswith("```"):
            flush_paragraph()
            if in_code:
                story.append(code_block("\n".join(code_lines), styles))
                code_lines.clear()
                in_code = False
            else:
                in_code = True
            i += 1
            continue

        if in_code:
            code_lines.append(line)
            i += 1
            continue

        if stripped.startswith("|") and i + 1 < len(lines) and is_table_separator(lines[i + 1]):
            flush_paragraph()
            rows = [split_table_row(line)]
            i += 2
            while i < len(lines) and lines[i].strip().startswith("|"):
                rows.append(split_table_row(lines[i]))
                i += 1
            story.extend([Spacer(1, 3), make_table(rows, styles), Spacer(1, 7)])
            continue

        heading = re.match(r"^(#{1,3})\s+(.+)$", stripped)
        if heading:
            flush_paragraph()
            level = len(heading.group(1))
            title = heading.group(2)
            if level == 1 and not skipped_title:
                skipped_title = True
                i += 1
                continue
            story.append(CondPageBreak(35 * mm))
            story.append(Paragraph(inline_markup(title), styles[f"h{level}"]))
            i += 1
            continue

        bullet = re.match(r"^[-*]\s+(.+)$", stripped)
        numbered = re.match(r"^(\d+)\.\s+(.+)$", stripped)
        if bullet or numbered:
            flush_paragraph()
            if bullet:
                prefix, content = "•", bullet.group(1)
            else:
                prefix, content = numbered.group(1) + ".", numbered.group(2)
            story.append(Paragraph(f"{prefix}&nbsp;&nbsp;{inline_markup(content)}", styles["bullet"]))
            i += 1
            continue

        if stripped in {"---", "***", "___"}:
            flush_paragraph()
            rule = Table([[""]], colWidths=[CONTENT_WIDTH], rowHeights=[1])
            rule.setStyle(TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#B0BEC5"))]))
            story.extend([Spacer(1, 5), rule, Spacer(1, 7)])
            i += 1
            continue

        if not stripped:
            flush_paragraph()
        else:
            paragraph.append(stripped)
        i += 1

    flush_paragraph()
    if in_code:
        story.append(code_block("\n".join(code_lines), styles))
    return story


def cover(styles):
    return [
        Spacer(1, 39 * mm),
        Paragraph("DOCUMENTACIÓN TÉCNICA", styles["cover_title"]),
        Spacer(1, 8 * mm),
        Paragraph("Proyecto de Servicios y Aplicaciones para IoT", styles["cover_subtitle"]),
        Spacer(1, 7 * mm),
        Table(
            [[""]],
            colWidths=[82 * mm],
            rowHeights=[1.6],
            hAlign="CENTER",
            style=TableStyle([("BACKGROUND", (0, 0), (-1, -1), colors.HexColor("#1565C0"))]),
        ),
        Spacer(1, 31 * mm),
        Paragraph("Aplicación inmobiliaria Android con Firebase y Supabase", styles["cover_meta"]),
        Spacer(1, 5 * mm),
        Paragraph("Documento de arquitectura, datos, seguridad, costos y manuales", styles["cover_meta"]),
        Spacer(1, 31 * mm),
        Paragraph("Revisión: julio de 2026", styles["cover_meta"]),
        PageBreak(),
    ]


def draw_page(canvas, doc):
    page = canvas.getPageNumber()
    canvas.saveState()
    if page > 1:
        canvas.setStrokeColor(colors.HexColor("#CFD8DC"))
        canvas.setLineWidth(0.5)
        canvas.line(LEFT, PAGE_HEIGHT - 15 * mm, PAGE_WIDTH - RIGHT, PAGE_HEIGHT - 15 * mm)
        canvas.setFont("DocRegular", 7.8)
        canvas.setFillColor(colors.HexColor("#607D8B"))
        canvas.drawString(LEFT, PAGE_HEIGHT - 12 * mm, "Documentación técnica del proyecto")
        canvas.drawRightString(PAGE_WIDTH - RIGHT, 11 * mm, f"Página {page}")
    canvas.restoreState()


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Uso: render_document.py entrada.md salida.pdf")
    source = Path(sys.argv[1]).resolve()
    output = Path(sys.argv[2]).resolve()
    output.parent.mkdir(parents=True, exist_ok=True)
    register_fonts()
    styles = build_styles()
    text = source.read_text(encoding="utf-8")
    story = cover(styles) + parse_markdown(text, styles)
    doc = SimpleDocTemplate(
        str(output),
        pagesize=A4,
        leftMargin=LEFT,
        rightMargin=RIGHT,
        topMargin=TOP,
        bottomMargin=BOTTOM,
        title="Documentación técnica del proyecto",
        author="Proyecto de Servicios y Aplicaciones para IoT",
        subject="Arquitectura, modelo de datos, seguridad, costos y manuales",
    )
    doc.build(story, onFirstPage=draw_page, onLaterPages=draw_page)
    print(output)


if __name__ == "__main__":
    main()
