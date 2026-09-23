using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Security.Cryptography;
using System.Text;

namespace Ressuscitou.Cifras.Tools
{
    /// <summary>Operações exclusivamente de desenvolvimento sobre cópias das fichas.</summary>
    public static class ImagemCifra
    {
        public static void Recortar(string origem, string destino, int x, int y, int largura, int altura)
        {
            using (var imagem = new Bitmap(origem))
            {
                var area = Rectangle.Intersect(new Rectangle(0, 0, imagem.Width, imagem.Height),
                    new Rectangle(x, y, Math.Max(1, largura), Math.Max(1, altura)));
                using (var recorte = imagem.Clone(area, PixelFormat.Format32bppArgb))
                {
                    recorte.Save(destino, ImageFormat.Png);
                }
            }
        }

        // Hash de máscara vermelha 16x16. Igualdade do hash é deliberadamente
        // conservadora: ela forma grupos para revisão, nunca dá nome a um acorde.
        public static string HashVisualVermelho(string arquivo)
        {
            using (var imagem = new Bitmap(arquivo))
            {
                var bits = new StringBuilder(256);
                for (var gy = 0; gy < 16; gy++)
                for (var gx = 0; gx < 16; gx++)
                {
                    var inicioX = gx * imagem.Width / 16;
                    var fimX = Math.Max(inicioX + 1, (gx + 1) * imagem.Width / 16);
                    var inicioY = gy * imagem.Height / 16;
                    var fimY = Math.Max(inicioY + 1, (gy + 1) * imagem.Height / 16);
                    var vermelhos = 0;
                    var total = 0;
                    for (var y = inicioY; y < fimY; y++)
                    for (var x = inicioX; x < fimX; x++)
                    {
                        var cor = imagem.GetPixel(x, y);
                        total++;
                        if (cor.R > cor.G + 30 && cor.R > cor.B + 20 && cor.R > 120) vermelhos++;
                    }
                    bits.Append(vermelhos * 5 >= total ? '1' : '0');
                }
                using (var sha = SHA256.Create())
                {
                    var bytes = sha.ComputeHash(Encoding.ASCII.GetBytes(bits.ToString()));
                    return BitConverter.ToString(bytes).Replace("-", "").ToLowerInvariant();
                }
            }
        }

        public static void RecortarNormalizado(string origem, string destino, double x, double y,
            double largura, double altura, double margemProporcional)
        {
            using (var imagem = new Bitmap(origem))
            {
                var margem = Math.Max(4, (int)Math.Ceiling(Math.Max(largura * imagem.Width, altura * imagem.Height) * margemProporcional));
                var inicioX = Math.Max(0, (int)Math.Floor(x * imagem.Width) - margem);
                var inicioY = Math.Max(0, (int)Math.Floor(y * imagem.Height) - margem);
                var fimX = Math.Min(imagem.Width, (int)Math.Ceiling((x + largura) * imagem.Width) + margem);
                var fimY = Math.Min(imagem.Height, (int)Math.Ceiling((y + altura) * imagem.Height) + margem);
                using (var crop = imagem.Clone(new Rectangle(inicioX, inicioY, Math.Max(1, fimX - inicioX), Math.Max(1, fimY - inicioY)), PixelFormat.Format32bppArgb))
                    crop.Save(destino, ImageFormat.Png);
            }
        }

        public static void PrepararAcorde(string origem, string destino, int escala, int margem)
        {
            using (var imagem = new Bitmap(origem))
            using (var preparado = new Bitmap(imagem.Width * escala + margem * 2, imagem.Height * escala + margem * 2, PixelFormat.Format24bppRgb))
            {
                using (var grafico = Graphics.FromImage(preparado)) grafico.Clear(Color.White);
                for (var y = 0; y < imagem.Height; y++)
                for (var x = 0; x < imagem.Width; x++)
                {
                    var cor = imagem.GetPixel(x, y);
                    if (!EhVermelho(cor)) continue;
                    for (var dy = 0; dy < escala; dy++)
                    for (var dx = 0; dx < escala; dx++) preparado.SetPixel(margem + x * escala + dx, margem + y * escala + dy, Color.Black);
                }
                preparado.Save(destino, ImageFormat.Png);
            }
        }

        public static void PrepararLetra(string origem, string destino)
        {
            using (var imagem = new Bitmap(origem))
            using (var preparado = new Bitmap(imagem.Width, imagem.Height, PixelFormat.Format24bppRgb))
            {
                for (var y = 0; y < imagem.Height; y++)
                for (var x = 0; x < imagem.Width; x++)
                {
                    var cor = imagem.GetPixel(x, y);
                    // Acordes vermelhos ficam brancos; o restante é binarizado sem
                    // corrigir conteúdo textual reconhecido posteriormente pelo OCR.
                    var luminosidade = (cor.R * 299 + cor.G * 587 + cor.B * 114) / 1000;
                    preparado.SetPixel(x, y, (!EhVermelho(cor) && luminosidade < 170) ? Color.Black : Color.White);
                }
                preparado.Save(destino, ImageFormat.Png);
            }
        }

        private static bool EhVermelho(Color cor)
        {
            return cor.R > cor.G + 30 && cor.R > cor.B + 20 && cor.R > 120;
        }
    }
}
