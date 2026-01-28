export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange
}) {
  const pageSizeOptions = [5, 10, 20, 50];

  const getPageNumbers = () => {
    const pages = [];
    const showPages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(showPages / 2));
    let endPage = Math.min(totalPages - 1, startPage + showPages - 1);

    if (endPage - startPage < showPages - 1) {
      startPage = Math.max(0, endPage - showPages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  };

  if (totalPages <= 1 && totalElements <= pageSize) {
    return null;
  }

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-4 px-4 py-3 bg-white border-t border-gray-200">
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-700">
          Showing <span className="font-medium">{currentPage * pageSize + 1}</span> to{' '}
          <span className="font-medium">
            {Math.min((currentPage + 1) * pageSize, totalElements)}
          </span>{' '}
          of <span className="font-medium">{totalElements}</span> results
        </span>

        <div className="flex items-center gap-2">
          <label className="text-sm text-gray-600">Per page:</label>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            className="px-2 py-1 border border-gray-300 rounded text-sm focus:ring-2 focus:ring-primary-500"
          >
            {pageSizeOptions.map((size) => (
              <option key={size} value={size}>
                {size}
              </option>
            ))}
          </select>
        </div>
      </div>

      <nav className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(0)}
          disabled={currentPage === 0}
          className="px-2 py-1 text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          title="First page"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>

        <button
          onClick={() => onPageChange(currentPage - 1)}
          disabled={currentPage === 0}
          className="px-2 py-1 text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Previous page"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        {getPageNumbers().map((page) => (
          <button
            key={page}
            onClick={() => onPageChange(page)}
            className={`px-3 py-1 text-sm rounded ${
              currentPage === page
                ? 'bg-primary-600 text-white'
                : 'text-gray-700 hover:bg-gray-100'
            }`}
          >
            {page + 1}
          </button>
        ))}

        <button
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="px-2 py-1 text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Next page"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>

        <button
          onClick={() => onPageChange(totalPages - 1)}
          disabled={currentPage >= totalPages - 1}
          className="px-2 py-1 text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Last page"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
          </svg>
        </button>
      </nav>
    </div>
  );
}
