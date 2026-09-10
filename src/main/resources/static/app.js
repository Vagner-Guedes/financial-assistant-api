const categories = [
    { code: 'FOOD', label: 'Alimentação' },
    { code: 'HEALTH', label: 'Saúde' },
    { code: 'TRANSPORT', label: 'Transporte' },
    { code: 'HOUSING', label: 'Moradia' },
    { code: 'LEISURE', label: 'Lazer' },
    { code: 'OTHER', label: 'Outros' }
];

const $ = (selector) => document.querySelector(selector);
let mediaRecorder;
let microphoneStream;
let recordingChunks = [];
let recordingInterval;
let recordingStartedAt;
let recordedBlob;
let recordingFileExtension = 'webm';
let recordingPreviewUrl;
let responseAudioUrl;

const api = (path, options) => fetch(path, options).then(async (response) => {
    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : await response.blob();
    if (!response.ok) {
        const message = body?.message || 'Não foi possível concluir a operação.';
        const details = body?.details ? ` ${Object.values(body.details).join(' ')}` : '';
        throw new Error(message + details);
    }
    return body;
});

function formatCurrency(value) {
    return Number(value || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatDate(value) {
    if (!value) return '—';
    return new Intl.DateTimeFormat('pt-BR').format(new Date(`${value}T12:00:00`));
}

function showToast(message, isError = false) {
    const toast = $('#toast');
    toast.textContent = message;
    toast.classList.toggle('error', isError);
    toast.classList.add('show');
    window.clearTimeout(showToast.timeout);
    showToast.timeout = window.setTimeout(() => toast.classList.remove('show'), 3600);
}

function populateCategories() {
    const createSelect = $('#category');
    const filterSelect = $('#filterCategory');
    categories.forEach(({ code, label }) => {
        createSelect.append(new Option(label, code));
        filterSelect.append(new Option(label, code));
    });
}

function currentFilters() {
    const params = new URLSearchParams();
    const category = $('#filterCategory').value;
    const from = $('#from').value;
    const to = $('#to').value;
    if (category) params.set('category', category);
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    return params;
}

async function loadDashboard() {
    const params = currentFilters();
    const suffix = params.toString() ? `?${params}` : '';
    try {
        const [summary, transactions] = await Promise.all([
            api(`/api/transactions/summary${suffix}`),
            api(`/api/transactions${suffix}`)
        ]);
        renderSummary(summary);
        renderTransactions(transactions);
        $('#connectionDot').className = 'connection-dot online';
        $('#connectionLabel').textContent = 'API conectada';
    } catch (error) {
        $('#connectionDot').className = 'connection-dot offline';
        $('#connectionLabel').textContent = 'API indisponível';
        showToast(error.message, true);
    }
}

function renderSummary(summary) {
    $('#totalAmount').textContent = formatCurrency(summary.totalAmount);
    $('#transactionCount').textContent = summary.transactionCount;
    const breakdown = Object.entries(summary.totalsByCategory || {});
    const top = breakdown.sort((a, b) => Number(b[1]) - Number(a[1]))[0];
    const topLabel = categories.find((category) => category.code === top?.[0])?.label;
    $('#topCategory').textContent = topLabel || '—';
    $('#topCategoryCaption').textContent = top ? `${formatCurrency(top[1])} no período` : 'Ainda sem dados';
    $('#totalCaption').textContent = summary.from || summary.to
        ? `${formatDate(summary.from)} até ${formatDate(summary.to)}`
        : 'Todos os lançamentos';
}

function renderTransactions(transactions) {
    const body = $('#transactionsBody');
    body.replaceChildren();
    $('#resultCount').textContent = `${transactions.length} resultado${transactions.length === 1 ? '' : 's'}`;
    if (!transactions.length) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 4;
        cell.className = 'empty-state';
        cell.textContent = 'Nenhuma transação encontrada para estes filtros.';
        row.append(cell);
        body.append(row);
        return;
    }
    transactions.forEach((transaction) => {
        const row = document.createElement('tr');
        const description = document.createElement('td');
        description.textContent = transaction.description;
        const category = document.createElement('td');
        const pill = document.createElement('span');
        pill.className = 'category-pill';
        pill.textContent = transaction.categoryLabel || transaction.category;
        category.append(pill);
        const date = document.createElement('td');
        date.textContent = formatDate(transaction.occurredAt);
        const amount = document.createElement('td');
        amount.className = 'align-right';
        amount.textContent = formatCurrency(transaction.amount);
        row.append(description, category, date, amount);
        body.append(row);
    });
}

async function loadAssistantStatus() {
    try {
        const status = await api('/api/assistant/status');
        $('#assistantBadge').textContent = status.available ? 'Ativo' : 'Configuração pendente';
        $('#assistantBadge').className = `status-badge ${status.available ? 'active' : 'error'}`;
        $('#assistantMessage').textContent = status.message;
        $('#voiceStatusText').textContent = status.available
            ? 'Assistente pronto para ouvir seu comando.'
            : 'Ative o perfil openai para usar o modo de voz.';
        $('#voiceDot').className = `connection-dot ${status.available ? 'online' : 'offline'}`;
        $('#recordButton').disabled = !status.available;
        if (!status.available) {
            $('#recordingHelp').textContent = 'A gravação será liberada quando o perfil openai estiver ativo.';
        }
    } catch (error) {
        $('#assistantBadge').textContent = 'Indisponível';
        $('#assistantBadge').className = 'status-badge error';
        $('#assistantMessage').textContent = 'Não foi possível consultar o status da IA.';
    }
}

function supportedRecordingType() {
    const types = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus'];
    return types.find((type) => window.MediaRecorder?.isTypeSupported(type)) || '';
}

function updateRecordingUi(isRecording) {
    const button = $('#recordButton');
    button.classList.toggle('recording', isRecording);
    $('#recordButtonLabel').textContent = isRecording ? 'Parar gravação' : 'Gravar pelo microfone';
    $('#recordingHelp').textContent = isRecording
        ? 'Gravando... fale naturalmente e clique para encerrar.'
        : 'O navegador pedirá permissão para acessar seu microfone.';
    $('#recordingHelp').classList.toggle('recording', isRecording);
}

function updateRecordingTimer() {
    const elapsed = Math.floor((Date.now() - recordingStartedAt) / 1000);
    const minutes = String(Math.floor(elapsed / 60)).padStart(2, '0');
    const seconds = String(elapsed % 60).padStart(2, '0');
    $('#recordingTimer').textContent = `${minutes}:${seconds}`;
    if (elapsed >= 60) stopRecording();
}

async function startRecording() {
    if (!navigator.mediaDevices?.getUserMedia || !window.MediaRecorder) {
        showToast('Seu navegador não oferece gravação de microfone.', true);
        return;
    }
    try {
        microphoneStream = await navigator.mediaDevices.getUserMedia({ audio: true });
        const mimeType = supportedRecordingType();
        mediaRecorder = mimeType ? new MediaRecorder(microphoneStream, { mimeType }) : new MediaRecorder(microphoneStream);
        recordingFileExtension = mimeType.includes('ogg') ? 'ogg' : 'webm';
        recordingChunks = [];
        mediaRecorder.addEventListener('dataavailable', (event) => {
            if (event.data.size > 0) recordingChunks.push(event.data);
        });
        mediaRecorder.addEventListener('stop', () => {
            recordedBlob = new Blob(recordingChunks, { type: mediaRecorder.mimeType || 'audio/webm' });
            if (recordingPreviewUrl) URL.revokeObjectURL(recordingPreviewUrl);
            recordingPreviewUrl = URL.createObjectURL(recordedBlob);
            $('#recordingPreview').src = recordingPreviewUrl;
            $('#recordingPreview').hidden = false;
            $('#fileName').textContent = 'Gravação do microfone pronta';
            $('#audioFile').value = '';
            if (microphoneStream) microphoneStream.getTracks().forEach((track) => track.stop());
            microphoneStream = undefined;
            showToast('Gravação pronta. Clique em Processar com IA.');
        });
        mediaRecorder.start();
        recordingStartedAt = Date.now();
        recordingInterval = window.setInterval(updateRecordingTimer, 250);
        updateRecordingUi(true);
    } catch (error) {
        if (microphoneStream) microphoneStream.getTracks().forEach((track) => track.stop());
        microphoneStream = undefined;
        showToast('Permita o acesso ao microfone no navegador para gravar.', true);
    }
}

function stopRecording() {
    if (!mediaRecorder || mediaRecorder.state === 'inactive') return;
    mediaRecorder.stop();
    window.clearInterval(recordingInterval);
    recordingInterval = undefined;
    $('#recordingTimer').textContent = '00:00';
    updateRecordingUi(false);
}

function toggleRecording() {
    if (mediaRecorder?.state === 'recording') stopRecording();
    else startRecording();
}

async function createTransaction(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = {
        description: $('#description').value.trim(),
        amount: Number($('#amount').value),
        category: $('#category').value,
        occurredAt: $('#occurredAt').value || null
    };
    try {
        await api('/api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        form.reset();
        $('#occurredAt').value = new Date().toISOString().slice(0, 10);
        showToast('Transação salva com sucesso.');
        await loadDashboard();
    } catch (error) {
        showToast(error.message, true);
    }
}

function prepareCommand(event) {
    event.preventDefault();
    const key = $('#apiKey').value.trim();
    if (!key) {
        showToast('Cole sua chave OpenAI para gerar o comando.', true);
        return;
    }
    const safeKey = key.replaceAll("'", "''");
    $('#startCommand').textContent = `$env:OPENAI_API_KEY = '${safeKey}'\n.\\gradlew.bat bootRun --args="--spring.profiles.active=openai"`;
    $('#commandBox').hidden = false;
    showToast('Comando pronto. Copie e execute no PowerShell.');
}

async function processVoice(event) {
    event.preventDefault();
    const file = $('#audioFile').files[0];
    const source = recordedBlob || file;
    if (!source) {
        showToast('Grave pelo microfone ou escolha um arquivo de áudio.', true);
        return;
    }
    const formData = new FormData();
    formData.append('file', source, recordedBlob ? `microphone.${recordingFileExtension}` : file.name);
    try {
        const result = await api('/api/assistant/voice', { method: 'POST', body: formData });
        if (responseAudioUrl) URL.revokeObjectURL(responseAudioUrl);
        responseAudioUrl = URL.createObjectURL(result);
        $('#responseAudio').src = responseAudioUrl;
        $('#responseAudio').hidden = false;
        $('#downloadAudio').href = responseAudioUrl;
        $('#downloadAudio').hidden = false;
        showToast('Comando processado. Ouça a resposta da IA.');
    } catch (error) {
        showToast(error.message, true);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    populateCategories();
    $('#occurredAt').value = new Date().toISOString().slice(0, 10);
    $('#transactionForm').addEventListener('submit', createTransaction);
    $('#filterForm').addEventListener('submit', (event) => { event.preventDefault(); loadDashboard(); });
    $('#clearFilters').addEventListener('click', () => {
        $('#filterForm').reset();
        loadDashboard();
    });
    $('#refreshDashboard').addEventListener('click', loadDashboard);
    $('#apiKeyForm').addEventListener('submit', prepareCommand);
    $('#toggleKey').addEventListener('click', () => {
        $('#apiKey').type = $('#apiKey').type === 'password' ? 'text' : 'password';
    });
    $('#copyCommand').addEventListener('click', async () => {
        await navigator.clipboard.writeText($('#startCommand').textContent);
        showToast('Comando copiado.');
    });
    $('#audioFile').addEventListener('change', (event) => {
        recordedBlob = undefined;
        if (recordingPreviewUrl) URL.revokeObjectURL(recordingPreviewUrl);
        $('#recordingPreview').hidden = true;
        $('#fileName').textContent = event.target.files[0]?.name || 'Escolha um áudio';
    });
    $('#recordButton').addEventListener('click', toggleRecording);
    $('#voiceForm').addEventListener('submit', processVoice);
    loadAssistantStatus();
    loadDashboard();
});

window.addEventListener('beforeunload', () => {
    if (mediaRecorder?.state === 'recording') mediaRecorder.stop();
    if (microphoneStream) microphoneStream.getTracks().forEach((track) => track.stop());
    if (recordingPreviewUrl) URL.revokeObjectURL(recordingPreviewUrl);
    if (responseAudioUrl) URL.revokeObjectURL(responseAudioUrl);
});
