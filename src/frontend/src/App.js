import './App.css';
import {PDFDocument} from 'pdf-lib';
import Docxtemplater from 'docxtemplater';
import PizZip from 'pizzip';
import pdfMake from 'pdfmake/build/pdfmake';
import pdfFonts from 'pdfmake/build/vfs_fonts';
import html2pdf from 'html2pdf.js';


pdfMake.vfs = pdfFonts.pdfMake.vfs;


function App() {
    const generateDocumentUrl = 'api/v1/schedule2/generateProductSheet';


    // const fetchData = async () => {
    //     try {
    //         const response = await fetch(generateDocumentUrl, {
    //             method: 'GET',
    //             headers: {
    //                 'Content-Type': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    //             },
    //         });
    //
    //         if (response.ok) {
    //             const blob = await response.blob();
    //             const pdfBytes = await convertWordToPDF(blob ); // Pass the desired filename
    //
    //             const pdfBlobUrl = window.URL.createObjectURL(new Blob([pdfBytes], {type: 'application/pdf'}));
    //
    //             await new Promise(resolve => setTimeout(resolve, 1000)); // Adjust the delay if needed
    //
    //
    //             const printWindow = window.open(pdfBlobUrl);
    //
    //
    //             printWindow.onload = function () {
    //                 printWindow.document.title = 'name.pdf';
    //                 printWindow.print();
    //
    //
    //                 window.URL.revokeObjectURL(pdfBlobUrl);
    //             };
    //         }
    //     } catch (error) {
    //         console.error('Error fetching or converting document:', error);
    //     }
    // };

    const fetchData = async () => {
        try {
            const response = await fetch(generateDocumentUrl, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch Word document. Status: ${response.status}`);
            }


            const wordBuffer = await response.arrayBuffer();

            const wordHtml = new TextDecoder('utf-8').decode(wordBuffer);

            // Open a new window and write the HTML content
            const printWindow = window.open('', '_blank');
            printWindow.document.write(wordHtml);

            // Wait for the content to be fully loaded before printing
            printWindow.onload = function () {
                printWindow.print();
                printWindow.onafterprint = function () {
                    printWindow.close();
                };
            };

        } catch (error) {
            console.error('Error fetching or converting document:', error);
        }
    };


    async function convertWordToPDF(wordBlob) {
        try {
            const wordArrayBuffer = await wordBlob.arrayBuffer();
            const wordUint8Array = new Uint8Array(wordArrayBuffer);

            if (!wordUint8Array || wordUint8Array.length === 0) {
                throw new Error('Invalid Word document buffer.');
            }

            const zip = new PizZip(wordUint8Array);
            const doc = new Docxtemplater(zip);

            const pdfBuffer = await new Promise((resolve, reject) => {
                pdfMake.createPdf(doc.getZip().generate({type: 'blob'})).getBuffer(buffer => {
                    if (buffer) {
                        resolve(buffer);
                    } else {
                        reject(new Error('Error generating PDF buffer.'));
                    }
                });
            });

            // Load the PDF buffer into a PDFDocument
            const pdfDoc = await PDFDocument.load(pdfBuffer);

            return pdfDoc.save();
        } catch (error) {
            console.error('Error converting Word to PDF:', error);
            throw error;
        }
    }

    async function convertWordToHTML(wordBuffer) {
        // Load the Word document using docxtemplater
        const zip = new PizZip(wordBuffer);
        const doc = new Docxtemplater().loadZip(zip);

        // Create a promise to handle asynchronous rendering
        return new Promise((resolve, reject) => {
            // Set up an event listener for the rendering completion
            doc.addListener('rendering-done', function (error) {
                if (error) {
                    reject(error);
                } else {
                    // Get the rendered HTML content
                    const renderedHTML = doc.getFullText();
                    resolve(renderedHTML);
                }
            });

            // Render the Word document asynchronously
            doc.render();
        });
    }


    return (
        <>
            <div>
                <button onClick={fetchData}>Generate, Download, and Print Document</button>
            </div>

        </>
    );
}

export default App;
