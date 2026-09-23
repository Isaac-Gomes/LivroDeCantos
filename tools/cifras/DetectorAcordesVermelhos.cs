using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

namespace Ressuscitou.Cifras.Tools
{
    public sealed class RegiaoVermelha
    {
        public float X { get; set; }
        public float Y { get; set; }
        public float Largura { get; set; }
        public float Altura { get; set; }
        public int PixelsVermelhos { get; set; }
    }

    /// <summary>
    /// Detector de desenvolvimento: localiza candidatos vermelhos, sem tentar
    /// reconhecer ou inferir acordes. A revisão humana continua obrigatória.
    /// </summary>
    public static class DetectorAcordesVermelhos
    {
        public static IList<RegiaoVermelha> Detectar(string arquivo)
        {
            using (var original = new Bitmap(arquivo))
            using (var bitmap = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb))
            using (var canvas = Graphics.FromImage(bitmap))
            {
                canvas.DrawImageUnscaled(original, 0, 0);
                return Detectar(bitmap);
            }
        }

        private static IList<RegiaoVermelha> Detectar(Bitmap bitmap)
        {
            var largura = bitmap.Width;
            var altura = bitmap.Height;
            var contagemPorLinha = new int[altura];
            var minimoPorLinha = new int[altura];
            var maximoPorLinha = new int[altura];
            for (var y = 0; y < altura; y++)
            {
                minimoPorLinha[y] = largura;
                maximoPorLinha[y] = -1;
            }

            var area = new Rectangle(0, 0, largura, altura);
            var dados = bitmap.LockBits(area, ImageLockMode.ReadOnly, PixelFormat.Format32bppArgb);
            try
            {
                var bytes = new byte[Math.Abs(dados.Stride) * altura];
                Marshal.Copy(dados.Scan0, bytes, 0, bytes.Length);
                for (var y = 0; y < altura; y++)
                {
                    var deslocamentoLinha = y * dados.Stride;
                    for (var x = 0; x < largura; x++)
                    {
                        var indice = deslocamentoLinha + x * 4;
                        var azul = bytes[indice];
                        var verde = bytes[indice + 1];
                        var vermelho = bytes[indice + 2];
                        if (EhVermelhoDeAcorde(vermelho, verde, azul))
                        {
                            contagemPorLinha[y]++;
                            if (x < minimoPorLinha[y]) minimoPorLinha[y] = x;
                            if (x > maximoPorLinha[y]) maximoPorLinha[y] = x;
                        }
                    }
                }
            }
            finally
            {
                bitmap.UnlockBits(dados);
            }

            var resultado = new List<RegiaoVermelha>();
            var inicio = -1;
            var fim = -1;
            for (var y = 0; y <= altura; y++)
            {
                var ativa = y < altura && contagemPorLinha[y] >= 3;
                if (ativa && inicio < 0)
                {
                    inicio = y;
                    fim = y;
                }
                else if (ativa && y - fim <= 5)
                {
                    fim = y;
                }
                else if (!ativa && inicio >= 0 && y - fim > 5)
                {
                    AdicionarFaixasDaLinha(resultado, inicio, fim, contagemPorLinha,
                        minimoPorLinha, maximoPorLinha, largura, altura);
                    inicio = -1;
                }
            }
            return resultado;
        }

        private static void AdicionarFaixasDaLinha(
            ICollection<RegiaoVermelha> resultado,
            int inicioY,
            int fimY,
            int[] contagemPorLinha,
            int[] minimoPorLinha,
            int[] maximoPorLinha,
            int larguraImagem,
            int alturaImagem)
        {
            var altura = fimY - inicioY + 1;
            if (altura < 5 || altura > 90) return;

            var contagemPorColuna = new int[larguraImagem];
            var total = 0;
            for (var y = inicioY; y <= fimY; y++)
            {
                if (contagemPorLinha[y] == 0) continue;
                for (var x = minimoPorLinha[y]; x <= maximoPorLinha[y]; x++)
                {
                    // A faixa horizontal delimita o candidato; a contagem exata
                    // não precisa distinguir cada glifo para servir à revisão.
                    contagemPorColuna[x]++;
                }
                total += contagemPorLinha[y];
            }

            var inicioX = -1;
            var fimX = -1;
            for (var x = 0; x <= larguraImagem; x++)
            {
                var ativa = x < larguraImagem && contagemPorColuna[x] > 0;
                if (ativa && inicioX < 0)
                {
                    inicioX = x;
                    fimX = x;
                }
                else if (ativa && x - fimX <= 18)
                {
                    fimX = x;
                }
                else if (!ativa && inicioX >= 0 && x - fimX > 18)
                {
                    var largura = fimX - inicioX + 1;
                    if (largura >= 4)
                    {
                        resultado.Add(new RegiaoVermelha
                        {
                            X = (float)inicioX / larguraImagem,
                            Y = (float)inicioY / alturaImagem,
                            Largura = (float)largura / larguraImagem,
                            Altura = (float)altura / alturaImagem,
                            PixelsVermelhos = total
                        });
                    }
                    inicioX = -1;
                }
            }
        }

        private static bool EhVermelhoDeAcorde(byte vermelho, byte verde, byte azul)
        {
            return vermelho > verde + 30 && vermelho > azul + 20 && vermelho > 120;
        }
    }
}
