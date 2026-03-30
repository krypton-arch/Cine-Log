import { createServer } from "node:http";
import { existsSync, readFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

loadLocalEnv();

const PORT = Number(process.env.PORT ?? 8787);
const ALLOW_ORIGIN = process.env.ALLOW_ORIGIN ?? "*";
const GEMINI_API_KEY = process.env.GEMINI_API_KEY ?? "";
const MODEL = process.env.GEMINI_MODEL ?? "gemini-2.5-flash";
const GEMINI_URL =
  `https://generativelanguage.googleapis.com/v1beta/models/${MODEL}:generateContent`;

const defaultHeaders = {
  "Access-Control-Allow-Origin": ALLOW_ORIGIN,
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

createServer(async (request, response) => {
  if (request.method === "OPTIONS") {
    response.writeHead(204, defaultHeaders);
    response.end();
    return;
  }

  if (request.method === "GET" && request.url === "/health") {
    sendJson(response, 200, {
      ok: true,
      configured: GEMINI_API_KEY.length > 0,
      model: MODEL,
    });
    return;
  }

  if (request.method === "POST" && request.url === "/v1/gemini/generate") {
    if (!GEMINI_API_KEY) {
      sendJson(response, 500, {
        error: "Gemini relay is missing GEMINI_API_KEY.",
      });
      return;
    }

    try {
      const body = await readJson(request);
      const systemPrompt = String(body?.systemPrompt ?? "").trim();
      const userMessage =
        String(body?.userMessage ?? "").trim() || "Reply to the system instruction.";

      if (!systemPrompt) {
        sendJson(response, 400, { error: "systemPrompt is required." });
        return;
      }

      const upstreamResponse = await fetch(
        `${GEMINI_URL}?key=${encodeURIComponent(GEMINI_API_KEY)}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            systemInstruction: {
              parts: [{ text: systemPrompt }],
            },
            contents: [
              {
                role: "user",
                parts: [{ text: userMessage }],
              },
            ],
            generationConfig: {
              temperature: 0.7,
              topP: 0.9,
              maxOutputTokens: 3072,
            },
          }),
        }
      );

      const rawBody = await upstreamResponse.text();
      if (!upstreamResponse.ok) {
        sendJson(response, upstreamResponse.status, {
          error: buildGeminiError(rawBody, upstreamResponse.status),
        });
        return;
      }

      const payload = safeJsonParse(rawBody);
      const text = extractText(payload);
      if (!text) {
        sendJson(response, 502, {
          error: "Gemini returned an empty response.",
        });
        return;
      }

      sendJson(response, 200, {
        text,
        model: MODEL,
      });
    } catch (error) {
      sendJson(response, 500, {
        error: error instanceof Error ? error.message : "Gemini relay failed.",
      });
    }
    return;
  }

  sendJson(response, 404, { error: "Not found." });
}).listen(PORT, () => {
  console.log(`Gemini relay listening on http://localhost:${PORT}`);
});

function sendJson(response, statusCode, body) {
  response.writeHead(statusCode, {
    ...defaultHeaders,
    "Content-Type": "application/json; charset=utf-8",
  });
  response.end(JSON.stringify(body));
}

async function readJson(request) {
  const chunks = [];
  for await (const chunk of request) {
    chunks.push(chunk);
  }

  const rawBody = Buffer.concat(chunks).toString("utf8");
  return rawBody ? JSON.parse(rawBody) : {};
}

function safeJsonParse(rawBody) {
  try {
    return JSON.parse(rawBody);
  } catch {
    return null;
  }
}

function extractText(payload) {
  return payload?.candidates
    ?.flatMap((candidate) => candidate?.content?.parts ?? [])
    ?.map((part) => part?.text?.trim?.())
    ?.find((text) => Boolean(text));
}

function buildGeminiError(rawBody, statusCode) {
  const payload = safeJsonParse(rawBody);
  const apiMessage = payload?.error?.message?.trim?.();
  return apiMessage
    ? `Gemini API error (${statusCode}): ${apiMessage}`
    : `Gemini API error (${statusCode}).`;
}

function loadLocalEnv() {
  const envPath = resolve(dirname(fileURLToPath(import.meta.url)), ".env");
  if (!existsSync(envPath)) return;

  const lines = readFileSync(envPath, "utf8").split(/\r?\n/);
  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;

    const separatorIndex = line.indexOf("=");
    if (separatorIndex <= 0) continue;

    const key = line.slice(0, separatorIndex).trim();
    const value = line.slice(separatorIndex + 1).trim();
    if (!process.env[key]) {
      process.env[key] = value;
    }
  }
}
