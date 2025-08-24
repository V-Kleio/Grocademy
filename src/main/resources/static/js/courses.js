document.addEventListener("DOMContentLoaded", function () {
	injectGlobalStyles();
	initGlobalSetup();

	if (document.querySelector(".courses-grid")) {
		initCourseListPage();
	} else if (document.querySelector(".course-detail-section")) {
		initCourseDetailPage();
	} else if (document.querySelector(".course-layout")) {
		initModulePage();
	}
});


function initGlobalSetup() {
	document.querySelectorAll(".btn").forEach((button) => {
		if (button.id === "mark-complete-btn") {
			button.addEventListener("click", function () {
				this.disabled = true;
				this.dataset.originalContent = this.innerHTML;
				this.innerHTML = "Processing...";
			});
		}
	});

	const searchInput = document.querySelector(".search-input");
	if (searchInput) {
		document.addEventListener("keydown", function (e) {
			if ((e.ctrlKey || e.metaKey) && e.key === "k") {
				e.preventDefault();
				searchInput.focus();
				searchInput.select();
			}
		});
	}
}

function showMessage(type, message) {
	const container = document.getElementById("message-container");
	if (!container) return;

	const alertDiv = document.createElement("div");
	alertDiv.className = `alert alert-${type}`;
	alertDiv.textContent = message;

	container.innerHTML = "";
	container.appendChild(alertDiv);

	setTimeout(() => {
		alertDiv.style.opacity = "0";
		setTimeout(() => alertDiv.remove(), 500);
	}, 4000);
}

function injectGlobalStyles() {
	const style = document.createElement("style");
	style.textContent = `
        mark {
            background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
            color: #92400e;
            padding: 0.1em 0.2em;
            border-radius: 3px;
            font-weight: 600;
        }
        .btn[disabled] {
            opacity: 0.7;
            pointer-events: none;
            cursor: not-allowed;
        }
        .alert {
            padding: 12px 16px;
            margin: 10px 0;
            border-radius: 6px;
            animation: fadeIn 0.3s ease;
            text-align: center;
        }
        .alert-success {
            background-color: #d1fae5;
            color: #065f46;
            border: 1px solid #6ee7b7;
        }
        .alert-error {
            background-color: #fee2e2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .content-panel {
            margin-top: 20px;
            position: relative;
            min-height: 100px;
        }
        .pdf-frame-loaded #pdf-loading,
        .video-loaded #video-loading {
            display: none;
        }
    `;
	document.head.appendChild(style);
}


function initCourseListPage() {
	console.log("Course List Page Initialized");
	const searchForm = document.querySelector(".search-form");
	const searchInput = document.querySelector(".search-input");
	const urlParams = new URLSearchParams(window.location.search);

	if (searchForm && searchInput) {
		if (urlParams.has("q")) {
			searchInput.focus();
		}
		searchForm.addEventListener("submit", function (e) {
			if (!searchInput.value.trim()) {
				e.preventDefault();
				window.location.href = window.location.pathname;
			}
		});
	}

	const observer = new IntersectionObserver(
		(entries) => {
			entries.forEach((entry, index) => {
				if (entry.isIntersecting) {
					entry.target.style.animation = `fadeInUp 0.6s ease forwards ${
						index * 50
					}ms`;
					observer.unobserve(entry.target);
				}
			});
		},
		{ threshold: 0.1 }
	);

	document.querySelectorAll(".course-card").forEach((card) => {
		card.style.opacity = "0";
		observer.observe(card);
	});

	document.querySelectorAll(".progress-fill").forEach((bar) => {
		const targetWidth = bar.style.width;
		bar.style.width = "0%";
		setTimeout(() => (bar.style.width = targetWidth), 300);
	});

	const searchQuery = urlParams.get("q");
	if (searchQuery) {
		const terms = searchQuery
			.toLowerCase()
			.split(" ")
			.filter((t) => t.length > 2);
		document.querySelectorAll(".course-card").forEach((card) => {
			[
				card.querySelector(".course-title"),
				card.querySelector(".course-description"),
			].forEach((el) => {
				if (el) {
					let html = el.innerHTML;
					terms.forEach((term) => {
						html = html.replace(
							new RegExp(`(${term})`, "gi"),
							"<mark>$1</mark>"
						);
					});
					el.innerHTML = html;
				}
			});
		});
	}
}


function initCourseDetailPage() {
	console.log("Course Detail Page Initialized");
	const buyForm = document.querySelector(".buy-form");
	if (buyForm) {
		buyForm.addEventListener("submit", function (e) {
			e.preventDefault();
			handleBuyCourse(this);
		});
	}
}

async function handleBuyCourse(form) {
	const submitBtn = form.querySelector(".buy-btn");

	submitBtn.disabled = true;
	submitBtn.dataset.originalContent = submitBtn.innerHTML;
	submitBtn.innerHTML = "Processing...";

	try {
		const courseIdMatch = form.action.match(/courses\/(\d+)\/buy/);
		if (!courseIdMatch || !courseIdMatch[1]) {
			throw new Error("Could not determine the course ID.");
		}
		const courseId = courseIdMatch[1];

		const response = await fetch(`/api/courses/${courseId}/buy`, {
			method: "POST",
			headers: {
			},
			credentials: "same-origin",
		});

		const data = await response.json();

		if (!response.ok) {
			throw new Error(data.message || "An unknown error occurred.");
		}

		showMessage(
			"success",
			data.message || "Course purchased! The page will now reload."
		);

		setTimeout(() => window.location.reload(), 2000);
	} catch (error) {
		console.error("Error purchasing course:", error);
		showMessage("error", error.message);

		submitBtn.disabled = false;
		submitBtn.innerHTML = submitBtn.dataset.originalContent;
	}
}


let currentModuleId = null;
let currentModuleData = null;

function initModulePage() {
	console.log("Module Page Initialized");

	if (
		!courseData ||
		!courseData.modules ||
		!Array.isArray(courseData.modules)
	) {
		console.error("Course data is not properly initialized:", courseData);
		showMessage(
			"error",
			"Error loading course data. Please refresh the page."
		);
		return;
	}

	console.log(`Course has ${courseData.modules.length} modules`);

	setupModuleListeners();

	const firstModuleElement = document.querySelector(".module-item");
	if (firstModuleElement) {
		selectModule(firstModuleElement);
	}
	animateProgress();
}

function setupModuleListeners() {
	document.querySelectorAll(".module-item").forEach((moduleItem) => {
		moduleItem.addEventListener("click", function () {
			selectModule(this);
		});
	});

	const pdfFrame = document.getElementById("pdf-frame");
	if (pdfFrame) {
		pdfFrame.addEventListener("load", function () {
			document
				.getElementById("pdf-viewer")
				.classList.add("pdf-frame-loaded");
			this.style.display = "block";
		});
	}

	const videoPlayer = document.getElementById("video-player");
	if (videoPlayer) {
		videoPlayer.addEventListener("loadeddata", function () {
			document
				.getElementById("video-viewer")
				.classList.add("video-loaded");
		});

		videoPlayer.addEventListener("error", function () {
			showMessage(
				"error",
				"Error loading video content. Please try again."
			);
		});
	}
}

function animateProgress() {
	const progressFill = document.querySelector(".progress-fill");
	if (progressFill) {
		const targetWidth = progressFill.style.width;
		progressFill.style.width = "0%";
		setTimeout(() => {
			progressFill.style.width = targetWidth;
		}, 300);
	}
}

function selectModule(moduleElement) {
	if (!moduleElement) {
		console.error("No module element provided");
		return;
	}

	try {
		const moduleId = moduleElement.dataset.moduleId;
		if (!moduleId) {
			console.error(
				"Module element doesn't have a moduleId data attribute"
			);
			return;
		}

		currentModuleId = moduleId;

		currentModuleData = courseData.modules.find(
			(m) => String(m.id) === String(moduleId)
		);
		if (!currentModuleData) {
			console.error(
				`Module with ID ${moduleId} not found in course data`
			);
			showMessage(
				"error",
				"Module data not found. Please refresh the page."
			);
			return;
		}

		console.log("Selected module:", currentModuleData);

		document
			.querySelectorAll(".module-item")
			.forEach((item) => item.classList.remove("active"));
		moduleElement.classList.add("active");

		document.getElementById("no-module-selected").style.display = "none";
		const viewer = document.getElementById("module-viewer");
		viewer.style.display = "block";

		document.getElementById("current-module-title").textContent =
			currentModuleData.title;
		document.getElementById("current-module-description").textContent =
			currentModuleData.description;

		updateCompletionUI();

		setupContentTabs();
	} catch (error) {
		console.error("Error selecting module:", error);
		showMessage(
			"error",
			"An error occurred while loading the module. Please try again."
		);
	}
}

function updateCompletionUI() {
	const markCompleteBtn = document.getElementById("mark-complete-btn");
	const completedBadge = document.getElementById("completed-badge");

	if (!currentModuleData) return;

	if (currentModuleData.completed) {
		markCompleteBtn.style.display = "none";
		completedBadge.style.display = "flex";
	} else {
		markCompleteBtn.style.display = "block";
		completedBadge.style.display = "none";
	}
}

function setupContentTabs() {
	if (!currentModuleData) return;

	const pdfTab = document.getElementById("pdf-tab");
	const videoTab = document.getElementById("video-tab");

	document.querySelectorAll(".content-panel").forEach((panel) => {
		panel.style.display = "none";
	});

	document.getElementById("pdf-frame").style.display = "none";
	document.getElementById("video-player").style.display = "none";

	const hasPdf = !!currentModuleData.pdfContent;
	const hasVideo = !!currentModuleData.videoContent;

	console.log(`Module content: PDF=${hasPdf}, Video=${hasVideo}`);

	pdfTab.style.display = hasPdf ? "flex" : "none";
	videoTab.style.display = hasVideo ? "flex" : "none";

	document.querySelectorAll(".content-tab").forEach((tab) => {
		tab.classList.remove("active");
	});

	if (hasPdf) {
		showContent("pdf");
		pdfTab.classList.add("active");
	} else if (hasVideo) {
		showContent("video");
		videoTab.classList.add("active");
	} else {
		showContent("none");
	}
}

function showContent(type) {
	document.querySelectorAll(".content-panel").forEach((panel) => {
		panel.style.display = "none";
	});

	document.querySelectorAll(".content-tab").forEach((tab) => {
		tab.classList.remove("active");
	});

	document.getElementById("no-content-message").style.display = "none";

	if (type === "pdf" && currentModuleData && currentModuleData.pdfContent) {
		const pdfViewer = document.getElementById("pdf-viewer");
		const pdfFrame = document.getElementById("pdf-frame");
		const pdfLoading = document.getElementById("pdf-loading");

		pdfViewer.style.display = "block";
		pdfViewer.classList.remove("pdf-frame-loaded");
		pdfLoading.style.display = "block";
		pdfFrame.style.display = "none";

		pdfFrame.src = currentModuleData.pdfContent;

		document.getElementById("pdf-tab").classList.add("active");
	} else if (
		type === "video" &&
		currentModuleData &&
		currentModuleData.videoContent
	) {
		const videoViewer = document.getElementById("video-viewer");
		const videoPlayer = document.getElementById("video-player");
		const videoLoading = document.getElementById("video-loading");

		videoViewer.style.display = "block";
		videoViewer.classList.remove("video-loaded");
		videoLoading.style.display = "block";

		videoPlayer.style.display = "block";
		videoPlayer.src = currentModuleData.videoContent;

		document.getElementById("video-tab").classList.add("active");
	} else {
		document.getElementById("no-content-message").style.display = "block";
	}
}

async function markModuleComplete() {
	if (!currentModuleId) {
		showMessage("error", "No module selected");
		return;
	}

	const markCompleteBtn = document.getElementById("mark-complete-btn");
	const originalContent = markCompleteBtn.innerHTML;

	try {
		markCompleteBtn.disabled = true;
		markCompleteBtn.innerHTML =
			'Marking...';

		const token =
			typeof getAuthToken === "function" ? getAuthToken() : null;

		const headers = {
			"Content-Type": "application/json",
		};

		if (token) {
			headers["Authorization"] = `Bearer ${token}`;
		}

		const response = await fetch(
			`/api/modules/${currentModuleId}/complete`,
			{
				method: "PATCH",
				headers: headers,
				credentials: "same-origin",
			}
		);

		if (!response.ok) {
			const errorData = await response.json().catch(() => ({
				message: `Server error: ${response.status} ${response.statusText}`,
			}));
			throw new Error(
				errorData.message || "Failed to mark module as complete"
			);
		}

		const data = await response.json();

		showMessage(
			"success",
			"Module marked as complete! Page will reload to update progress."
		);


		setTimeout(() => window.location.reload(), 1500);
	} catch (error) {
		console.error("Error marking module complete:", error);
		showMessage(
			"error",
			error.message || "Failed to mark module as complete"
		);

		markCompleteBtn.disabled = false;
		markCompleteBtn.innerHTML = originalContent;
	}
}
